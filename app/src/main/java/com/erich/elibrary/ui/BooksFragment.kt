package com.erich.elibrary.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erich.elibrary.ApiClient
import com.erich.elibrary.Book
import com.erich.elibrary.Category
import com.erich.elibrary.R
import com.erich.elibrary.Repo
import com.erich.elibrary.databinding.FragmentBooksBinding
import com.erich.elibrary.errorMessage
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BooksFragment : Fragment(R.layout.fragment_books) {

    private var _b: FragmentBooksBinding? = null
    private val b get() = _b!!

    private val adapter = BookAdapter { download(it) }
    private val items = mutableListOf<Book>()
    private var categories: List<Category> = emptyList()

    private var page = 1
    private var lastPage = 1
    private var loading = false
    private var requestId = 0 // ignores answers to outdated searches
    private var query = ""
    private var category: String? = null
    private var searchJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentBooksBinding.bind(view)

        b.recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        b.recycler.adapter = adapter
        b.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val last = (rv.layoutManager as GridLayoutManager).findLastVisibleItemPosition()
                if (dy > 0 && last >= adapter.itemCount - 4) loadMore()
            }
        })

        b.search.doAfterTextChanged { text ->
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(350) // wait until typing pauses
                query = text?.toString().orEmpty().trim()
                reload()
            }
        }

        b.btnLogin.setOnClickListener { openAuth("login") }
        b.btnSignUp.setOnClickListener { openAuth("register") }
        b.btnLogout.setOnClickListener { viewLifecycleOwner.lifecycleScope.launch { Repo.logout() } }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { Repo.user.collect { renderHeader() } }
                launch { Repo.status.collect { renderHeader() } }
            }
        }

        adapter.submitList(items.toList())
        renderChips()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (!Repo.initialized) Repo.init()
                if (categories.isEmpty()) {
                    categories = ApiClient.api.categories()
                    renderChips()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                toast(errorMessage(e))
            }
            if (items.isEmpty()) reload()
        }
    }

    private fun openAuth(mode: String) {
        findNavController().navigate(R.id.authFragment, bundleOf("mode" to mode))
    }

    private fun renderHeader() {
        val loggedIn = Repo.user.value != null
        _b?.apply {
            btnLogin.isVisible = !loggedIn
            btnSignUp.isVisible = !loggedIn
            btnLogout.isVisible = loggedIn
            val s = Repo.status.value
            counter.isVisible = s != null
            if (s != null) {
                counter.text = "${if (loggedIn) "Member" else "Guest"}: ${s.remaining} of ${s.limit} downloads left"
            }
        }
    }

    private fun renderChips() {
        val group = b.chips
        group.setOnCheckedStateChangeListener(null)
        group.removeAllViews()
        addChip("All", null)
        categories.forEach { addChip("${it.category} (${it.total})", it.category) }

        // Restore the current selection, then listen for changes.
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            chip.isChecked = (chip.tag as String?) == category
        }
        group.setOnCheckedStateChangeListener { g, ids ->
            val chip = ids.firstOrNull()?.let { g.findViewById<Chip>(it) } ?: return@setOnCheckedStateChangeListener
            category = chip.tag as String?
            reload()
        }
    }

    private fun addChip(label: String, value: String?) {
        val chip = layoutInflater.inflate(R.layout.chip_filter, b.chips, false) as Chip
        chip.id = View.generateViewId()
        chip.text = label
        chip.tag = value
        b.chips.addView(chip)
    }

    private fun reload() {
        requestId++
        page = 1
        lastPage = 1
        load()
    }

    private fun loadMore() {
        if (!loading && page < lastPage) {
            page++
            load()
        }
    }

    private fun load() {
        loading = true
        b.progress.isVisible = true
        val id = requestId
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val r = ApiClient.api.books(query.ifBlank { null }, category, page)
                if (id != requestId) return@launch
                if (page == 1) items.clear()
                items.addAll(r.data)
                lastPage = r.lastPage
                adapter.submitList(items.toList())
                b.countText.text = "${r.total} ${if (r.total == 1) "book" else "books"}"
                b.empty.isVisible = items.isEmpty()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (id == requestId) {
                    if (page > 1) page--
                    toast(errorMessage(e))
                }
            } finally {
                if (id == requestId) {
                    loading = false
                    _b?.progress?.isVisible = false
                }
            }
        }
    }

    private fun download(book: Book) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val d = Repo.download(book)
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(d.downloadUrl)))
                toast("Downloading “${book.title}”")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                toast(errorMessage(e)) // e.g. "Guest download limit reached (5). Register to get more."
            }
        }
    }

    private fun toast(message: String) {
        context?.let { Toast.makeText(it, message, Toast.LENGTH_LONG).show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

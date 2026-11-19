package com.erich.elibrary.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.erich.elibrary.Book
import com.erich.elibrary.LibraryItem
import com.erich.elibrary.R
import com.erich.elibrary.Repo
import com.erich.elibrary.databinding.FragmentLibraryBinding
import com.erich.elibrary.errorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** My Library: saved books and favorites, synced with the website through the API. */
class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _b: FragmentLibraryBinding? = null
    private val b get() = _b!!

    private var onlyFavorites = false

    private val adapter = LibraryAdapter(
        onDownload = { download(it) },
        onFavorite = { toggleFavorite(it) },
        onRemove = { remove(it) },
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentLibraryBinding.bind(view)

        b.recycler.layoutManager = LinearLayoutManager(requireContext())
        b.recycler.adapter = adapter

        b.toggle.check(R.id.btnAll)
        b.toggle.addOnButtonCheckedListener { _, id, checked ->
            if (checked) {
                onlyFavorites = id == R.id.btnFav
                render()
            }
        }
        b.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.authFragment, bundleOf("mode" to "login"))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    Repo.user.collect { user ->
                        render()
                        if (user != null) refresh() // pick up changes made on the website
                    }
                }
                launch { Repo.library.collect { render() } }
            }
        }
    }

    private fun refresh() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Repo.loadLibrary()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                toast(errorMessage(e))
            }
        }
    }

    private fun render() {
        val v = _b ?: return
        val loggedIn = Repo.user.value != null
        v.guestPrompt.isVisible = !loggedIn
        v.content.isVisible = loggedIn
        if (!loggedIn) return

        val all = Repo.library.value
        val shown = if (onlyFavorites) all.filter { it.isFavorite } else all
        adapter.submitList(shown)

        v.btnAll.text = "All (${all.size})"
        v.btnFav.text = "Favorites (${all.count { it.isFavorite }})"
        v.empty.text = if (onlyFavorites) {
            "No favorites yet.\nMark a saved book as a favorite."
        } else {
            "Nothing saved yet.\nChoose Save on any book in the catalog."
        }
        v.empty.isVisible = shown.isEmpty()
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
                toast(errorMessage(e)) // the API's own message at the limit
            }
        }
    }

    private fun toggleFavorite(item: LibraryItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Repo.toggleFavorite(item.bookId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                toast(errorMessage(e))
            }
        }
    }

    private fun remove(item: LibraryItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Repo.removeBook(item.bookId)
                toast("Removed from My Library")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                toast(errorMessage(e))
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

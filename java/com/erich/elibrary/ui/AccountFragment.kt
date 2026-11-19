package com.erich.elibrary.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.erich.elibrary.R
import com.erich.elibrary.Repo
import com.erich.elibrary.databinding.FragmentAccountBinding
import kotlinx.coroutines.launch

/** Who is signed in, the download counter, and log in / sign up / log out. */
class AccountFragment : Fragment(R.layout.fragment_account) {

    private var _b: FragmentAccountBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentAccountBinding.bind(view)

        b.btnLogin.setOnClickListener { openAuth("login") }
        b.btnSignUp.setOnClickListener { openAuth("register") }
        b.btnLogout.setOnClickListener { viewLifecycleOwner.lifecycleScope.launch { Repo.logout() } }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { Repo.user.collect { render() } }
                launch { Repo.status.collect { render() } }
            }
        }
    }

    private fun openAuth(mode: String) {
        findNavController().navigate(R.id.authFragment, bundleOf("mode" to mode))
    }

    private fun render() {
        val v = _b ?: return
        val user = Repo.user.value
        val loggedIn = user != null

        if (user != null) {
            v.accountName.text = user.name
            v.accountInfo.text = user.email
        } else {
            v.accountName.text = "Browsing as a guest"
            v.accountInfo.text = "Log in to save books to My Library and get 10 downloads."
        }

        val s = Repo.status.value
        v.counter.isVisible = s != null
        if (s != null) {
            v.counter.text = "${if (loggedIn) "Member" else "Guest"}: ${s.remaining} of ${s.limit} downloads left"
        }

        v.btnLogin.isVisible = !loggedIn
        v.btnSignUp.isVisible = !loggedIn
        v.btnLogout.isVisible = loggedIn
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

package com.erich.elibrary.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.erich.elibrary.R
import com.erich.elibrary.Repo
import com.erich.elibrary.databinding.FragmentAuthBinding
import com.erich.elibrary.errorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** One screen for both log in and register, chosen by the "mode" argument. */
class AuthFragment : Fragment(R.layout.fragment_auth) {

    private var _b: FragmentAuthBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentAuthBinding.bind(view)
        val register = arguments?.getString("mode") == "register"

        b.title.text = if (register) "Create your account" else "Welcome back"
        b.subtitle.text = if (register) {
            "Accounts get 10 downloads instead of 5, plus a library that follows you across devices."
        } else {
            "Log in to use your 10 downloads and your library."
        }
        b.nameLayout.isVisible = register
        b.confirmLayout.isVisible = register
        b.submit.text = if (register) "Create account" else "Log in"
        b.switchLink.text = if (register) "Already have an account? Log in" else "New here? Create an account"

        b.submit.setOnClickListener { submit(register) }

        b.switchLink.setOnClickListener {
            findNavController().navigate(
                R.id.authFragment,
                bundleOf("mode" to if (register) "login" else "register"),
                navOptions { popUpTo(R.id.authFragment) { inclusive = true } },
            )
        }
        b.guestLink.setOnClickListener { findNavController().popBackStack(R.id.booksFragment, false) }
    }

    private fun submit(register: Boolean) {
        val name = b.nameInput.text.toString().trim()
        val email = b.emailInput.text.toString().trim()
        val password = b.passwordInput.text.toString()
        val confirm = b.confirmInput.text.toString()

        b.error.isVisible = false
        b.submit.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (register) Repo.register(name, email, password, confirm) else Repo.login(email, password)
                findNavController().popBackStack(R.id.booksFragment, false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _b?.error?.text = errorMessage(e)
                _b?.error?.isVisible = true
            } finally {
                _b?.submit?.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}

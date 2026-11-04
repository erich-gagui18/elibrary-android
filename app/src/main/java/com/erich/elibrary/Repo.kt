package com.erich.elibrary

import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.HttpException

/** Shared app state: who is logged in and how many downloads are left. */
object Repo {
    private val api get() = ApiClient.api

    val user = MutableStateFlow<User?>(null)
    val status = MutableStateFlow<DownloadStatus?>(null)
    var initialized = false
        private set

    suspend fun init() {
        if (Session.token != null) {
            try {
                user.value = api.me().user
            } catch (e: HttpException) {
                if (e.code() == 401) Session.token = null else throw e
            }
        }
        if (Session.token == null && Session.guestToken == null) startGuest()
        refreshStatus()
        initialized = true
    }

    private suspend fun startGuest() {
        Session.guestToken = api.startGuest().token
    }

    suspend fun refreshStatus() {
        try {
            status.value = api.status()
        } catch (e: HttpException) {
            // Stale guest token: start a fresh guest session once and retry.
            if (Session.token == null && e.code() == 401) {
                startGuest()
                status.value = api.status()
            } else throw e
        }
    }

    suspend fun login(email: String, password: String) = applyAuth(api.login(LoginRequest(email, password)))

    suspend fun register(name: String, email: String, password: String, confirmation: String) =
        applyAuth(api.register(RegisterRequest(name, email, password, confirmation)))

    private suspend fun applyAuth(response: AuthResponse) {
        Session.token = response.token
        user.value = response.user
        refreshStatus()
    }

    suspend fun logout() {
        runCatching { api.logout() } // token may already be invalid: still clear it locally
        Session.token = null
        user.value = null
        runCatching { refreshStatus() }
    }

    /** Limits are enforced by the API (HTTP 429), never in the app. */
    suspend fun download(book: Book): DownloadResponse {
        try {
            val d = api.download(book.id)
            status.value = DownloadStatus(d.limit, d.used, d.remaining)
            return d
        } catch (e: HttpException) {
            if (e.code() == 429) runCatching { refreshStatus() }
            throw e
        }
    }
}

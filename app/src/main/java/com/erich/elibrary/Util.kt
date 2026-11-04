package com.erich.elibrary

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/** Turns any failure into a message a person can act on. */
fun errorMessage(e: Throwable): String = when (e) {
    is HttpException -> {
        val raw = e.response()?.errorBody()?.string()
        val parsed = runCatching { Gson().fromJson(raw, ApiMessage::class.java) }.getOrNull()
        parsed?.errors?.values?.firstOrNull()?.firstOrNull()
            ?: parsed?.message
            ?: "Request failed (${e.code()})."
    }
    is IOException -> "Cannot reach the server. Check that the API is running."
    else -> e.message ?: "Something went wrong."
}

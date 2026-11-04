package com.erich.elibrary

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** Keeps the login token and guest token on the device. */
object Session {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("elibrary", Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs.getString("token", null)
        set(value) = prefs.edit { if (value == null) remove("token") else putString("token", value) }

    var guestToken: String?
        get() = prefs.getString("guest", null)
        set(value) = prefs.edit { if (value == null) remove("guest") else putString("guest", value) }
}

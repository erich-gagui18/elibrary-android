package com.erich.elibrary

import android.app.Application

class ELibraryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Session.init(this)
    }
}

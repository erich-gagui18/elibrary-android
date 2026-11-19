package com.erich.elibrary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bottom tabs: each menu item id matches a destination id in nav_graph.xml.
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        findViewById<BottomNavigationView>(R.id.bottom_nav).setupWithNavController(navHost.navController)

        // Keep content clear of the status bar, navigation bar and keyboard.
        // The tab bar hides while the keyboard is open so it does not sit on top of it.
        val tabs = findViewById<BottomNavigationView>(R.id.bottom_nav)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            val keyboardOpen = insets.isVisible(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, if (keyboardOpen) keyboard.bottom else bars.bottom)
            tabs.isVisible = !keyboardOpen
            insets
        }
    }
}

package com.practicum.playlistmaker.presentation.main

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.practicum.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isDark = viewModel.isDarkTheme()
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(view.paddingLeft, statusBar.top, view.paddingRight, view.paddingBottom)

            view.post { syncBottomNavigationWithNavDestination() }

            insets
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, _ ->
            window.decorView.post { syncBottomNavigationWithNavDestination() }
            applyDefaultStatusBar()
        }
        syncBottomNavigationWithNavDestination()
        applyDefaultStatusBar()
    }

    fun syncBottomNavigationWithNavDestination() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val divider = findViewById<View>(R.id.rectangle_8)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val destId = navHostFragment?.navController?.currentDestination?.id

        val content = findViewById<View>(android.R.id.content)
        val imeBottom = ViewCompat.getRootWindowInsets(content)
            ?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0

        if (imeBottom > 0) {
            bottomNav.visibility = View.GONE
            divider?.visibility = View.GONE
            return
        }

        when (destId) {
            R.id.audioPlayerFragment, R.id.createPlaylistFragment, R.id.editPlaylistFragment, R.id.playlistDetailFragment -> {
                bottomNav.visibility = View.GONE
                divider?.visibility = View.GONE
            }
            else -> {
                bottomNav.visibility = View.VISIBLE
                divider?.visibility = View.VISIBLE
            }
        }
    }

    private fun applyDefaultStatusBar() {
        val nightMode =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = true
            window.isNavigationBarContrastEnforced = true
        }
        window.navigationBarColor = ContextCompat.getColor(
            this,
            if (nightMode) R.color.yp_black else R.color.white
        )
        if (nightMode) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.yp_black)
            controller.isAppearanceLightStatusBars = false
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            controller.isAppearanceLightStatusBars = true
        }
    }
}
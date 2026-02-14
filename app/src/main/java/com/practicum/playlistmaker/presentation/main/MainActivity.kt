package com.practicum.playlistmaker.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(view.paddingLeft, statusBar.top, view.paddingRight, view.paddingBottom)

            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            val divider = findViewById<View>(R.id.rectangle_8)
            if (ime.bottom > 0) {
                bottomNavigationView.visibility = View.GONE
                divider?.visibility = View.GONE
            } else {
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                val navController = navHostFragment?.navController
                navController?.let {
                    when (it.currentDestination?.id) {
                        R.id.audioPlayerFragment -> {
                            bottomNavigationView.visibility = View.GONE
                            divider?.visibility = View.GONE
                        }

                        else -> {
                            bottomNavigationView.visibility = View.VISIBLE
                            divider?.visibility = View.VISIBLE
                        }
                    }
                }

            }

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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            val divider = findViewById<View>(R.id.rectangle_8)
            when (destination.id) {
                R.id.audioPlayerFragment -> {
                    bottomNav.visibility = View.GONE
                    divider?.visibility = View.GONE
                }
                else -> {
                    bottomNav.visibility = View.VISIBLE
                    divider?.visibility = View.VISIBLE
                }
            }
        }
    }
    }
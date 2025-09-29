package com.practicum.playlistmaker

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    fun applySavedTheme(context: Context) {
        val isDark = ThemeStorage(context).isDarkTheme()
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun setDarkTheme(context: Context, enabled: Boolean) {
        ThemeStorage(context).setDarkTheme(enabled)
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
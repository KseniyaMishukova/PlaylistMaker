package com.practicum.playlistmaker

import android.content.Context
import android.content.SharedPreferences

class ThemeStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDarkTheme(): Boolean =
        prefs.getBoolean(KEY_IS_DARK, false)

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DARK, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "playlistmaker_prefs"
        private const val KEY_IS_DARK = "is_dark_theme"
    }
}
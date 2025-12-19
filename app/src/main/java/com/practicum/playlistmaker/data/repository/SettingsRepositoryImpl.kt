package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.practicum.playlistmaker.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val prefs: SharedPreferences
) : SettingsRepository {

    override fun isDarkTheme(): Boolean =
        prefs.getBoolean(KEY_IS_DARK, false)

    override fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DARK, enabled).apply()
    }

    companion object {
        const val PREFS_NAME = "playlistmaker_prefs"
        private const val KEY_IS_DARK = "is_dark_theme"
    }
}
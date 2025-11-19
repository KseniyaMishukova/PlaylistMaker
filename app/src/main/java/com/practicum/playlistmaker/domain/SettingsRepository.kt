package com.practicum.playlistmaker.domain

interface SettingsRepository {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
}
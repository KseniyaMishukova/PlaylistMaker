package com.practicum.playlistmaker.domain.repository

interface SettingsRepository {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
}
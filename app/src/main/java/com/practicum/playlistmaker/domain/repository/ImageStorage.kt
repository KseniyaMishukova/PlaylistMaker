package com.practicum.playlistmaker.domain.repository

import android.net.Uri

interface ImageStorage {
    fun copyToAppStorage(uri: Uri): String?
}

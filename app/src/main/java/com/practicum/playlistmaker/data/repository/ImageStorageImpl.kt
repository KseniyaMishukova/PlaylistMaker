package com.practicum.playlistmaker.data.repository

import android.content.Context
import android.net.Uri
import com.practicum.playlistmaker.data.utils.ImageStorageHelper
import com.practicum.playlistmaker.domain.repository.ImageStorage

class ImageStorageImpl(
    private val context: Context
) : ImageStorage {

    override fun copyToAppStorage(uri: Uri): String? =
        ImageStorageHelper.copyToAppStorage(context, uri)
}

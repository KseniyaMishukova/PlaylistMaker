package com.practicum.playlistmaker.data.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorageHelper {

    fun copyToAppStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val extension = context.contentResolver.getType(sourceUri)?.substringAfter("/") ?: "jpg"
            val fileName = "cover_${UUID.randomUUID()}.$extension"
            val destFile = File(context.filesDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}

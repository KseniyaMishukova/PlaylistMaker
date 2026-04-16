package com.practicum.playlistmaker.data.utils

fun parseTrackTimeToMillis(trackTime: String?): Long {
    if (trackTime.isNullOrBlank()) return 0L
    val parts = trackTime.split(":").mapNotNull { it.trim().toIntOrNull() }
    return when (parts.size) {
        2 -> (parts[0] * 60L + parts[1]) * 1000L
        3 -> (parts[0] * 3600L + parts[1] * 60L + parts[2]) * 1000L
        else -> 0L
    }
}

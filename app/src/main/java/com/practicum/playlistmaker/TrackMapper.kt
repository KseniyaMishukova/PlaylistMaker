package com.practicum.playlistmaker

import java.text.SimpleDateFormat
import java.util.Locale

fun TrackDto.toDomain(): Track {
    val duration = SimpleDateFormat("mm:ss", Locale.getDefault())
        .format((trackTimeMillis ?: 0L))
    return Track(
        trackId = trackId ?: 0L,
        trackName = trackName.orEmpty(),
        artistName = artistName.orEmpty(),
        trackTime = duration,
        artworkUrl100 = artworkUrl100.orEmpty()
    )
}
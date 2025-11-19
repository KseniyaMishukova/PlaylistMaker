package com.practicum.playlistmaker

import java.text.SimpleDateFormat
import java.util.Locale

fun TrackDto.toDomain(): Track? {

    val id = trackId ?: return null

    val duration = SimpleDateFormat("mm:ss", Locale.getDefault())
        .format((trackTimeMillis ?: 0L))

    val year = releaseDate
        ?.takeIf { it.length >= 4 }
        ?.substring(0, 4)

    return Track(
        trackId = id,
        trackName = trackName.orEmpty(),
        artistName = artistName.orEmpty(),
        trackTime = duration,
        artworkUrl100 = artworkUrl100.orEmpty(),
        collectionName = collectionName,
        releaseYear = year,
        primaryGenreName = primaryGenreName.orEmpty(),
        country = country.orEmpty(),
        previewUrl = previewUrl.orEmpty()
    )
}
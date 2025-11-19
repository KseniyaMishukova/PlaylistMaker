package com.practicum.playlistmaker.domain

import java.io.Serializable

data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTime: String,
    val artworkUrl100: String,
    val collectionName: String?,
    val releaseYear: String?,
    val primaryGenreName: String,
    val country: String,
    val previewUrl: String
) : Serializable {
    fun getCoverArtwork(): String = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
}
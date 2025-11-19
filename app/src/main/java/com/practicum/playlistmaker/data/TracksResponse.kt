package com.practicum.playlistmaker.data

data class TracksResponse(
    val resultCount: Int,
    val results: List<TrackDto>
)
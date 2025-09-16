package com.practicum.playlistmaker

data class TracksResponse(
    val resultCount: Int,
    val results: List<TrackDto>
)
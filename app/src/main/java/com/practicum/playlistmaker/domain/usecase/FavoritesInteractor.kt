package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesInteractor {
    suspend fun addTrack(track: Track)
    suspend fun removeTrack(track: Track)
    fun getFavoriteTracks(): Flow<List<Track>>
    suspend fun isFavorite(trackId: Long): Boolean
}
package com.practicum.playlistmaker.data.repository

import java.text.SimpleDateFormat
import java.util.Locale

import com.practicum.playlistmaker.data.db.FavoriteTracksDao
import com.practicum.playlistmaker.data.db.TrackEntity
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteTracksDao
) : FavoritesRepository {

    override suspend fun addTrack(track: Track) {
        dao.addTrack(
            TrackEntity(
                trackId = track.trackId.toInt(),
                artworkUrl100 = track.artworkUrl100,
                trackName = track.trackName,
                artistName = track.artistName,
                collectionName = track.collectionName,
                releaseDate = track.releaseYear,
                primaryGenreName = track.primaryGenreName,
                country = track.country,
                trackTime = track.trackTime,
                previewUrl = track.previewUrl,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun removeTrack(track: Track) {
        dao.removeTrack(
            TrackEntity(
                trackId = track.trackId.toInt(),
                artworkUrl100 = track.artworkUrl100,
                trackName = track.trackName,
                artistName = track.artistName,
                collectionName = track.collectionName,
                releaseDate = track.releaseYear,
                primaryGenreName = track.primaryGenreName,
                country = track.country,
                trackTime = track.trackTime,
                previewUrl = track.previewUrl
            )
        )
    }

    override fun getFavoriteTracks(): Flow<List<Track>> = dao.getFavoriteTracks().map { entities ->
        entities.map { entity ->
            Track(
                trackId = entity.trackId.toLong(),
                trackName = entity.trackName ?: "",
                artistName = entity.artistName ?: "",
                trackTime = entity.trackTime ?: "00:00",
                artworkUrl100 = entity.artworkUrl100 ?: "",
                collectionName = entity.collectionName,
                releaseYear = entity.releaseDate?.split("-")?.get(0),
                primaryGenreName = entity.primaryGenreName ?: "",
                country = entity.country ?: "",
                previewUrl = entity.previewUrl ?: ""
            ).apply {
                isFavorite = true
            }
        }
    }
    
    override suspend fun getFavoriteTrackIds(): List<Int> {
        return dao.getFavoriteTrackIds()
    }
}
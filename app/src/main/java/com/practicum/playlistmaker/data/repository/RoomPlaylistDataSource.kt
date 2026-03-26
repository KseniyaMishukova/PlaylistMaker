package com.practicum.playlistmaker.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.db.PlaylistDao
import com.practicum.playlistmaker.data.db.PlaylistEntity
import com.practicum.playlistmaker.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.data.db.PlaylistTracksDao
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.PlaylistDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPlaylistDataSource(
    private val playlistDao: PlaylistDao,
    private val playlistTracksDao: PlaylistTracksDao,
    private val gson: Gson
) : PlaylistDataSource {

    private val trackIdsType = object : TypeToken<List<Long>>() {}.type

    override suspend fun insertPlaylist(playlist: Playlist): Long {
        val entity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverPath = playlist.coverPath,
            trackIdsJson = gson.toJson(playlist.trackIds),
            trackCount = playlist.trackCount
        )
        return playlistDao.insert(entity)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> =
        playlistDao.getAllPlaylists().map { entities ->
            entities.map { mapEntity(it) }
        }

    override suspend fun insertPlaylistTrack(track: Track) {
        playlistTracksDao.addTrack(
            PlaylistTrackEntity(
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

    override suspend fun getPlaylistById(id: Long): Playlist? {
        val entity = playlistDao.getPlaylistById(id) ?: return null
        return mapEntity(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverPath = playlist.coverPath,
            trackIdsJson = gson.toJson(playlist.trackIds),
            trackCount = playlist.trackCount
        )
        playlistDao.update(entity)
    }

    private fun mapEntity(entity: PlaylistEntity): Playlist {
        val trackIds: List<Long> = gson.fromJson(entity.trackIdsJson, trackIdsType) ?: emptyList()
        return Playlist(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            coverPath = entity.coverPath,
            trackIds = trackIds,
            trackCount = entity.trackCount
        )
    }
}

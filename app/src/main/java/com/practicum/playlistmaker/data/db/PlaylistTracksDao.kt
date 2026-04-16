package com.practicum.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistTracksDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrack(track: PlaylistTrackEntity)

    @Query("SELECT * FROM playlist_tracks")
    fun getAllTracks(): Flow<List<PlaylistTrackEntity>>

    @Query("DELETE FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun deleteByTrackId(trackId: Int)
}

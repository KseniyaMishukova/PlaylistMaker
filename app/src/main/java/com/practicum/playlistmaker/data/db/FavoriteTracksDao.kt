package com.practicum.playlistmaker.data.db

import androidx.room.*
import com.practicum.playlistmaker.data.db.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTracksDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrack(track: TrackEntity)

    @Delete
    suspend fun removeTrack(track: TrackEntity)

    @Query("SELECT * FROM favorite_tracks ORDER BY timestamp DESC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getFavoriteTrackIds(): List<Int>
}
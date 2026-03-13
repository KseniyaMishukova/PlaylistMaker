package com.practicum.playlistmaker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class], version = 5, exportSchema = false)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoriteTracksDao(): FavoriteTracksDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTracksDao(): PlaylistTracksDao
}
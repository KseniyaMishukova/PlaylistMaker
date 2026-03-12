package com.practicum.playlistmaker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TrackEntity::class], version = 3, exportSchema = false)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoriteTracksDao(): FavoriteTracksDao
}
package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.domain.repository.HistoryRepository
import com.practicum.playlistmaker.domain.models.Track
import com.google.gson.Gson

class HistoryRepositoryImpl(
    private val prefs: SharedPreferences,
    private val gson: Gson
) : HistoryRepository {

    private val listType = object : TypeToken<ArrayList<Track>>() {}.type

    override fun getHistory(): List<Track> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            gson.fromJson<ArrayList<Track>>(json, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun addTrack(track: Track) {
        val list = ArrayList(getHistory())

        val existingIndex = list.indexOfFirst { it.trackId == track.trackId }
        if (existingIndex >= 0) {
            list.removeAt(existingIndex)
        }

        list.add(0, track)

        if (list.size > MAX_SIZE) {
            while (list.size > MAX_SIZE) {
                list.removeAt(list.lastIndex)
            }
        }

        save(list)
    }

    override fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun save(list: List<Track>) {
        val json = gson.toJson(list, listType)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    companion object {
        const val PREFS_NAME = "playlistmaker_prefs"
        private const val KEY_HISTORY = "search_history"
        private const val MAX_SIZE = 10
    }
}
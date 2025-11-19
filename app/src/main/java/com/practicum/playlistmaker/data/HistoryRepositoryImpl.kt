package com.practicum.playlistmaker.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.domain.HistoryRepository
import com.practicum.playlistmaker.domain.Track

class HistoryRepositoryImpl(
    context: Context
) : HistoryRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()
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
        private const val PREFS_NAME = "playlistmaker_prefs"
        private const val KEY_HISTORY = "search_history"
        private const val MAX_SIZE = 10
    }
}
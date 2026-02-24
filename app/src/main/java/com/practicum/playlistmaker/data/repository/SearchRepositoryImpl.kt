package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.dto.TracksResponse
import com.practicum.playlistmaker.data.network.ITunesApi
import com.practicum.playlistmaker.data.utils.toDomain
import com.practicum.playlistmaker.domain.repository.SearchRepository
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.data.db.FavoriteTracksDao
import retrofit2.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchRepositoryImpl(
    private val api: ITunesApi,
    private val favoriteTracksDao: FavoriteTracksDao
) : SearchRepository {

    override fun searchTracks(query: String): Flow<Result<List<Track>>> = flow {
        try {
            val response = api.search(query)
            if (response.isSuccessful) {
                val tracks = response.body()?.results?.mapNotNull { it.toDomain() }.orEmpty()
                emit(Result.success(tracks))
            } else {
                emit(Result.failure(Exception("Response error: ${response.code()}")))
            }
        } catch (t: Throwable) {
            emit(Result.failure(t))
        }

    }
}
package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.dto.TracksResponse
import com.practicum.playlistmaker.data.network.ITunesApi
import com.practicum.playlistmaker.data.utils.toDomain
import com.practicum.playlistmaker.domain.repository.SearchRepository
import com.practicum.playlistmaker.domain.models.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchRepositoryImpl(
    private val api: ITunesApi
) : SearchRepository {

    override fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit) {
        api.search(query).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(
                call: Call<TracksResponse>,
                response: Response<TracksResponse>
            ) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.results?.mapNotNull { it.toDomain() }.orEmpty()
                    callback(Result.success(tracks))
                } else {
                    callback(Result.failure(Exception("Response error: ${response.code()}")))
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }
}
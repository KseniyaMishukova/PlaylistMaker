package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.dto.TracksResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface ITunesApi {
    @GET("/search?entity=song")
    suspend fun search(@Query("term") text: String): Response<TracksResponse>
}
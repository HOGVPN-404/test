package com.example.coba.data.remote

import com.example.coba.data.remote.dto.ApiResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET(".")
    suspend fun browse(
        @Query("url") shareUrl: String,
        @Query("fid") folderId: String? = null,
        @Query("pwd") password: String? = null
    ): ApiResponseDto
}

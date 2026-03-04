package com.example.coba.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponseDto(
    @SerialName("status") val status: String = "error",
    @SerialName("msg") val msg: String? = null,
    @SerialName("current_folder_id") val currentFolderId: String? = null,
    @SerialName("total_items") val totalItems: Int = 0,
    @SerialName("data") val data: List<ApiItemDto>? = null
)

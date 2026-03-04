package com.example.coba.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiItemDto(
    @SerialName("filename") val filename: String = "",
    @SerialName("size_mb") val sizeMb: String = "0",
    @SerialName("is_folder") val isFolder: Boolean = false,
    @SerialName("fs_id") val fsId: JsonElement? = null,
    @SerialName("thumb") val thumb: String? = null,
    @SerialName("links") val links: ApiLinksDto? = null
)

@Serializable
data class ApiLinksDto(
    @SerialName("browse") val browse: String? = null,
    @SerialName("original") val original: String? = null,
    @SerialName("proxy") val proxy: String? = null
)

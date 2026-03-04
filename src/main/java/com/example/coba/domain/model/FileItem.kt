package com.example.coba.domain.model

import com.example.coba.core.Constants

data class FileItem(
    val filename: String,
    val sizeMb: String,
    val isFolder: Boolean,
    val fsId: String,
    val thumb: String?,
    val browseLink: String?,
    val originalLink: String?,
    val proxyLink: String?
) {
    val preferredDownloadUrl: String?
        get() = proxyLink?.takeIf { it.isNotBlank() } ?: originalLink?.takeIf { it.isNotBlank() }

    val isVideo: Boolean
        get() = Constants.VideoExtensions.any { filename.lowercase().endsWith(it) }
}

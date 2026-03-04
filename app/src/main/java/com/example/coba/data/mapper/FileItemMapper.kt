package com.example.coba.data.mapper

import com.example.coba.data.remote.dto.ApiItemDto
import com.example.coba.domain.model.FileItem
import javax.inject.Inject
import kotlinx.serialization.json.JsonPrimitive

class FileItemMapper @Inject constructor() {
    fun map(dto: ApiItemDto): FileItem {
        val fsIdString = when (val value = dto.fsId) {
            is JsonPrimitive -> value.content
            null -> ""
            else -> value.toString()
        }
        return FileItem(
            filename = dto.filename,
            sizeMb = dto.sizeMb,
            isFolder = dto.isFolder,
            fsId = fsIdString,
            thumb = dto.thumb,
            browseLink = dto.links?.browse,
            originalLink = dto.links?.original,
            proxyLink = dto.links?.proxy
        )
    }
}

package com.example.coba.ui.navigation

import android.net.Uri
import com.example.coba.domain.model.FileItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class FileNavPayload(
    val filename: String,
    val sizeMb: String,
    val fsId: String,
    val original: String? = null,
    val proxy: String? = null
) {
    val preferredUrl: String?
        get() = proxy?.takeIf { it.isNotBlank() } ?: original?.takeIf { it.isNotBlank() }
}

object NavPayload {
    private val json = Json { ignoreUnknownKeys = true }

    fun fromItem(item: FileItem): FileNavPayload {
        return FileNavPayload(
            filename = item.filename,
            sizeMb = item.sizeMb,
            fsId = item.fsId,
            original = item.originalLink,
            proxy = item.proxyLink
        )
    }

    fun encode(payload: FileNavPayload): String = json.encodeToString(payload)

    fun decode(encoded: String): FileNavPayload {
        return json.decodeFromString(Uri.decode(encoded))
    }
}

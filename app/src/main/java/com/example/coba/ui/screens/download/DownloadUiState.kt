package com.example.coba.ui.screens.download

import com.example.coba.ui.navigation.FileNavPayload
import java.util.UUID

enum class DownloadStatus {
    IDLE,
    QUEUED,
    DOWNLOADING,
    SUCCESS,
    FAILED
}

data class DownloadUiState(
    val file: FileNavPayload? = null,
    val status: DownloadStatus = DownloadStatus.IDLE,
    val progress: Int = 0,
    val currentWorkId: UUID? = null,
    val outputUri: String? = null,
    val errorMessage: String? = null
)

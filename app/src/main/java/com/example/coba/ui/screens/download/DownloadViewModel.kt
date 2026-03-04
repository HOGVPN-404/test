package com.example.coba.ui.screens.download

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.getWorkInfoByIdFlow
import androidx.work.workDataOf
import com.example.coba.download.DownloadWorker
import com.example.coba.ui.navigation.Destination
import com.example.coba.ui.navigation.NavPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DownloadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        val rawPayload = savedStateHandle.get<String>(Destination.DOWNLOAD_ARG)
        if (rawPayload.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Data file tidak ditemukan") }
        } else {
            runCatching {
                NavPayload.decode(rawPayload)
            }.onSuccess { payload ->
                _uiState.update { it.copy(file = payload) }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = "Payload file tidak valid") }
            }
        }
    }

    fun startDownload() {
        val file = _uiState.value.file ?: return
        val downloadUrl = file.preferredUrl
        if (downloadUrl.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Link download tidak tersedia") }
            return
        }

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_URL to downloadUrl,
                    DownloadWorker.KEY_FILENAME to file.filename
                )
            )
            .addTag("download_${file.fsId}")
            .build()

        val uniqueWorkName = "download_${file.fsId}_${file.filename.hashCode()}"
        workManager.enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, request)
        observeWork(request.id)
        _uiState.update {
            it.copy(
                status = DownloadStatus.QUEUED,
                progress = 0,
                errorMessage = null,
                currentWorkId = request.id
            )
        }
    }

    private fun observeWork(workId: UUID) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(workId).collect { info ->
                if (info == null) return@collect
                val progress = info.progress.getInt(DownloadWorker.KEY_PROGRESS, 0)
                when (info.state) {
                    WorkInfo.State.ENQUEUED -> {
                        _uiState.update { it.copy(status = DownloadStatus.QUEUED, progress = progress) }
                    }

                    WorkInfo.State.RUNNING -> {
                        _uiState.update { it.copy(status = DownloadStatus.DOWNLOADING, progress = progress) }
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        _uiState.update {
                            it.copy(
                                status = DownloadStatus.SUCCESS,
                                progress = 100,
                                outputUri = info.outputData.getString(DownloadWorker.KEY_OUTPUT_URI)
                            )
                        }
                    }

                    WorkInfo.State.FAILED,
                    WorkInfo.State.CANCELLED -> {
                        _uiState.update {
                            it.copy(
                                status = DownloadStatus.FAILED,
                                errorMessage = info.outputData.getString(DownloadWorker.KEY_ERROR)
                                    ?: "Download gagal",
                                progress = progress
                            )
                        }
                    }

                    WorkInfo.State.BLOCKED -> Unit
                }
            }
        }
    }
}

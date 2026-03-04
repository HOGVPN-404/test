package com.example.coba.ui.screens.download

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coba.core.Constants

@Composable
fun DownloadRoute(
    onBack: () -> Unit,
    onPlay: (url: String, filename: String) -> Unit,
    viewModel: DownloadViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DownloadScreen(
        state = state,
        onBack = onBack,
        onDownload = viewModel::startDownload,
        onPlay = onPlay
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadScreen(
    state: DownloadUiState,
    onBack: () -> Unit,
    onDownload: () -> Unit,
    onPlay: (String, String) -> Unit
) {
    val file = state.file
    val context = LocalContext.current
    val askNotificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onDownload()
    }
    val startDownloadAction = remember(context, onDownload) {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionState = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                if (permissionState != PackageManager.PERMISSION_GRANTED) {
                    askNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onDownload()
                }
            } else {
                onDownload()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = file?.filename ?: "File tidak ditemukan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ukuran: ${file?.sizeMb ?: "-"} MB",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Status: ${state.status.name.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            when (state.status) {
                DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> {
                    LinearProgressIndicator(
                        progress = (state.progress.coerceIn(0, 100)) / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Progress ${state.progress}%")
                }

                DownloadStatus.SUCCESS -> {
                    Text(
                        text = "Download selesai: ${state.outputUri ?: "tersimpan di Downloads"}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                DownloadStatus.FAILED -> {
                    Text(
                        text = state.errorMessage ?: "Download gagal",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                DownloadStatus.IDLE -> Unit
            }

            Button(
                onClick = startDownloadAction,
                enabled = file?.preferredUrl?.isNotBlank() == true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Download")
            }

            if (file?.filename?.let { name ->
                    Constants.VideoExtensions.any { ext -> name.endsWith(ext, true) }
                } == true && !file.preferredUrl.isNullOrBlank()
            ) {
                OutlinedButton(
                    onClick = { onPlay(file.preferredUrl.orEmpty(), file.filename) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Play")
                }
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kembali")
            }
        }
    }
}

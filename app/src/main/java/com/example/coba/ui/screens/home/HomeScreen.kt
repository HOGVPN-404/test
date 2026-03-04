package com.example.coba.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coba.domain.model.FileItem
import com.example.coba.ui.common.UiState
import com.example.coba.ui.components.BreadcrumbBar
import com.example.coba.ui.components.EmptyStateView
import com.example.coba.ui.components.ErrorStateView
import com.example.coba.ui.components.FileItemCard
import com.example.coba.ui.components.LoadingSkeletonList

@Composable
fun HomeRoute(
    onOpenDownload: (FileItem) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    HomeScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onSearchChange = viewModel::onSearchChange,
        onRefresh = viewModel::refresh,
        onFolderClick = viewModel::openFolder,
        onFileClick = onOpenDownload,
        onBreadcrumbClick = viewModel::navigateToBreadcrumb,
        onRetry = viewModel::reload,
        onPasswordChange = viewModel::onPasswordInputChange,
        onPasswordDismiss = viewModel::dismissPasswordDialog,
        onPasswordSubmit = viewModel::submitPassword
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onFolderClick: (FileItem) -> Unit,
    onFileClick: (FileItem) -> Unit,
    onBreadcrumbClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordDismiss: () -> Unit,
    onPasswordSubmit: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Home") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            BreadcrumbBar(
                nodes = state.breadcrumb,
                onNodeClick = onBreadcrumbClick,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.query,
                onValueChange = onSearchChange,
                singleLine = true,
                placeholder = { Text("Cari file / folder") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = state.listState,
                    label = "home_list_state"
                ) { uiState ->
                    when (uiState) {
                        UiState.Loading -> LoadingSkeletonList()
                        is UiState.Empty -> EmptyStateView(uiState.message)
                        is UiState.Error -> ErrorStateView(
                            message = uiState.message,
                            onRetry = onRetry
                        )

                        is UiState.Success -> {
                            val filtered = uiState.data.filter {
                                it.filename.contains(state.query, ignoreCase = true)
                            }
                            if (filtered.isEmpty()) {
                                EmptyStateView("Tidak ada hasil untuk \"${state.query}\"")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(filtered, key = { "${it.fsId}_${it.filename}" }) { item ->
                                        FileItemCard(
                                            item = item,
                                            onClick = {
                                                if (it.isFolder) onFolderClick(it) else onFileClick(it)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showPasswordDialog) {
        AlertDialog(
            onDismissRequest = onPasswordDismiss,
            title = { Text("Folder Butuh Password") },
            text = {
                OutlinedTextField(
                    value = state.passwordInput,
                    onValueChange = onPasswordChange,
                    placeholder = { Text("Masukkan password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = onPasswordSubmit) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = onPasswordDismiss) {
                    Text("Batal")
                }
            }
        )
    }
}

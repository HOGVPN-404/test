package com.example.coba.ui.screens.home

import com.example.coba.domain.model.FileItem
import com.example.coba.ui.common.UiState

data class BreadcrumbNode(
    val id: String?,
    val label: String
)

data class HomeUiState(
    val listState: UiState<List<FileItem>> = UiState.Loading,
    val breadcrumb: List<BreadcrumbNode> = listOf(BreadcrumbNode(id = null, label = "Root")),
    val query: String = "",
    val isRefreshing: Boolean = false,
    val showPasswordDialog: Boolean = false,
    val passwordInput: String = "",
    val snackbarMessage: String? = null
)

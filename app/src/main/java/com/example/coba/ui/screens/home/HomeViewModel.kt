package com.example.coba.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coba.domain.model.BrowseResult
import com.example.coba.domain.model.FileItem
import com.example.coba.domain.usecase.GetFolderItemsUseCase
import com.example.coba.domain.usecase.SetSessionPasswordUseCase
import com.example.coba.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFolderItemsUseCase: GetFolderItemsUseCase,
    private val setSessionPasswordUseCase: SetSessionPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentFolder(initialLoad = true)
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun openFolder(item: FileItem) {
        if (!item.isFolder) return
        _uiState.update {
            it.copy(
                breadcrumb = it.breadcrumb + BreadcrumbNode(
                    id = item.fsId,
                    label = item.filename
                ),
                query = ""
            )
        }
        loadCurrentFolder(showLoading = true)
    }

    fun navigateToBreadcrumb(index: Int) {
        val current = _uiState.value.breadcrumb
        if (index !in current.indices) return
        _uiState.update {
            it.copy(
                breadcrumb = current.take(index + 1),
                query = ""
            )
        }
        loadCurrentFolder(showLoading = true)
    }

    fun refresh() {
        loadCurrentFolder(refresh = true)
    }

    fun reload() {
        loadCurrentFolder(showLoading = true)
    }

    fun onPasswordInputChange(value: String) {
        _uiState.update { it.copy(passwordInput = value) }
    }

    fun dismissPasswordDialog() {
        _uiState.update {
            it.copy(
                showPasswordDialog = false,
                listState = UiState.Error("Password dibutuhkan untuk membuka folder ini.")
            )
        }
    }

    fun submitPassword() {
        val password = _uiState.value.passwordInput.trim()
        if (password.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "Password tidak boleh kosong") }
            return
        }
        setSessionPasswordUseCase(password)
        _uiState.update { it.copy(showPasswordDialog = false) }
        loadCurrentFolder(showLoading = true)
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun loadCurrentFolder(
        initialLoad: Boolean = false,
        refresh: Boolean = false,
        showLoading: Boolean = false
    ) {
        viewModelScope.launch {
            if (initialLoad || showLoading) {
                _uiState.update { it.copy(listState = UiState.Loading) }
            } else if (refresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            }

            val folderId = _uiState.value.breadcrumb.lastOrNull()?.id
            when (val result = getFolderItemsUseCase(folderId)) {
                is BrowseResult.Success -> {
                    val state = if (result.items.isEmpty()) {
                        UiState.Empty("Folder ini kosong.")
                    } else {
                        UiState.Success(result.items)
                    }
                    _uiState.update {
                        it.copy(
                            listState = state,
                            isRefreshing = false,
                            showPasswordDialog = false
                        )
                    }
                }

                is BrowseResult.RequiresPassword -> {
                    _uiState.update {
                        it.copy(
                            showPasswordDialog = true,
                            isRefreshing = false,
                            snackbarMessage = result.message
                        )
                    }
                }

                is BrowseResult.WrongPassword -> {
                    _uiState.update {
                        it.copy(
                            showPasswordDialog = true,
                            passwordInput = "",
                            isRefreshing = false,
                            snackbarMessage = result.message
                        )
                    }
                }

                is BrowseResult.Error -> {
                    _uiState.update {
                        it.copy(
                            listState = UiState.Error(result.message),
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }
}

package com.example.coba.domain.model

sealed interface BrowseResult {
    data class Success(
        val currentFolderId: String,
        val items: List<FileItem>
    ) : BrowseResult

    data class RequiresPassword(val message: String) : BrowseResult
    data class WrongPassword(val message: String) : BrowseResult
    data class Error(val message: String) : BrowseResult
}

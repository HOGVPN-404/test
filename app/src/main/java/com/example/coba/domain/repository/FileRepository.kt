package com.example.coba.domain.repository

import com.example.coba.domain.model.BrowseResult

interface FileRepository {
    suspend fun browseFolder(folderId: String? = null): BrowseResult
    fun setSessionPassword(password: String?)
}

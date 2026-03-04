package com.example.coba.domain.usecase

import com.example.coba.domain.model.BrowseResult
import com.example.coba.domain.repository.FileRepository
import javax.inject.Inject

class GetFolderItemsUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(folderId: String?): BrowseResult = repository.browseFolder(folderId)
}

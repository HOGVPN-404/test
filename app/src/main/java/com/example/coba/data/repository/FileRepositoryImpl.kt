package com.example.coba.data.repository

import com.example.coba.BuildConfig
import com.example.coba.data.mapper.FileItemMapper
import com.example.coba.data.remote.ApiService
import com.example.coba.domain.model.BrowseResult
import com.example.coba.domain.repository.FileRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class FileRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val mapper: FileItemMapper
) : FileRepository {

    private var sessionPassword: String? = BuildConfig.PWD_DEFAULT.takeIf { it.isNotBlank() }

    override suspend fun browseFolder(folderId: String?): BrowseResult = withContext(Dispatchers.IO) {
        try {
            val response = apiService.browse(
                shareUrl = BuildConfig.BASE_SHARE_URL,
                folderId = folderId,
                password = sessionPassword
            )

            if (response.status.equals("success", ignoreCase = true)) {
                return@withContext BrowseResult.Success(
                    currentFolderId = response.currentFolderId ?: folderId ?: "root",
                    items = response.data.orEmpty().map(mapper::map)
                )
            }

            val message = response.msg.orEmpty()
            return@withContext when {
                message.contains("Butuh Password", ignoreCase = true) -> {
                    BrowseResult.RequiresPassword(message)
                }

                message.contains("Password Salah", ignoreCase = true) -> {
                    sessionPassword = null
                    BrowseResult.WrongPassword(message)
                }

                else -> BrowseResult.Error(message.ifBlank { "Gagal memuat folder" })
            }
        } catch (io: IOException) {
            BrowseResult.Error(io.message ?: "Koneksi gagal")
        } catch (e: Exception) {
            BrowseResult.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    override fun setSessionPassword(password: String?) {
        sessionPassword = password?.trim()?.takeIf { it.isNotBlank() }
    }
}

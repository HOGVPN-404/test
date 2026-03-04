package com.example.coba.data.repository

import com.example.coba.data.mapper.FileItemMapper
import com.example.coba.data.remote.ApiService
import com.example.coba.data.remote.dto.ApiItemDto
import com.example.coba.data.remote.dto.ApiResponseDto
import com.example.coba.domain.model.BrowseResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FileRepositoryImplTest {

    private val fakeApi = FakeApiService()
    private val repository = FileRepositoryImpl(
        apiService = fakeApi,
        mapper = FileItemMapper()
    )

    @Test
    fun `browseFolder returns success and mapped items`() = runTest {
        fakeApi.nextResponse = ApiResponseDto(
            status = "success",
            currentFolderId = "root",
            data = listOf(
                ApiItemDto(
                    filename = "video.mp4",
                    sizeMb = "10.5",
                    isFolder = false,
                    fsId = JsonPrimitive("123")
                )
            )
        )

        repository.setSessionPassword("1234")
        val result = repository.browseFolder("root")

        assertTrue(result is BrowseResult.Success)
        result as BrowseResult.Success
        assertEquals(1, result.items.size)
        assertEquals("1234", fakeApi.lastPassword)
    }

    @Test
    fun `browseFolder returns requires password`() = runTest {
        fakeApi.nextResponse = ApiResponseDto(
            status = "error",
            msg = "Butuh Password (&pwd=...)"
        )

        val result = repository.browseFolder(null)

        assertTrue(result is BrowseResult.RequiresPassword)
    }

    @Test
    fun `browseFolder returns wrong password`() = runTest {
        fakeApi.nextResponse = ApiResponseDto(
            status = "error",
            msg = "Password Salah (Err: -12)"
        )

        val result = repository.browseFolder(null)

        assertTrue(result is BrowseResult.WrongPassword)
    }

    private class FakeApiService : ApiService {
        var nextResponse: ApiResponseDto = ApiResponseDto(status = "success", data = emptyList())
        var lastPassword: String? = null

        override suspend fun browse(
            shareUrl: String,
            folderId: String?,
            password: String?
        ): ApiResponseDto {
            lastPassword = password
            return nextResponse
        }
    }
}

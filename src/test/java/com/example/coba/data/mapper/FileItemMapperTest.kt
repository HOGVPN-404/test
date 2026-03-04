package com.example.coba.data.mapper

import com.example.coba.data.remote.dto.ApiItemDto
import com.example.coba.data.remote.dto.ApiLinksDto
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.serialization.json.JsonPrimitive

class FileItemMapperTest {

    private val mapper = FileItemMapper()

    @Test
    fun `map should convert dto to domain model`() {
        val dto = ApiItemDto(
            filename = "movie.mp4",
            sizeMb = "123.4",
            isFolder = false,
            fsId = JsonPrimitive("991"),
            thumb = "https://cdn/thumb.jpg",
            links = ApiLinksDto(
                original = "https://origin/file",
                proxy = "https://proxy/file"
            )
        )

        val result = mapper.map(dto)

        assertEquals("movie.mp4", result.filename)
        assertEquals("123.4", result.sizeMb)
        assertEquals("991", result.fsId)
        assertEquals("https://proxy/file", result.proxyLink)
        assertEquals(true, result.isVideo)
    }
}

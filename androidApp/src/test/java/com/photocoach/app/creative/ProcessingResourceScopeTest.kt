package com.photocoach.app.creative

import java.io.IOException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ProcessingResourceScopeTest {
    @Test
    fun `temporary file creation failure still releases source and target`() {
        val recycled = mutableListOf<String>()
        val deleted = mutableListOf<String>()

        assertThrows(IOException::class.java) {
            resourceScope(recycled, deleted).use { resources ->
                resources.ownSource("source")
                resources.ownTarget("target")
                throw IOException("temporary file creation failed")
            }
        }

        assertEquals(listOf("target", "source"), recycled)
        assertEquals(emptyList<String>(), deleted)
    }

    @Test
    fun `render or save failure deletes temporary file and releases both bitmaps`() {
        val recycled = mutableListOf<String>()
        val deleted = mutableListOf<String>()

        assertThrows(IOException::class.java) {
            resourceScope(recycled, deleted).use { resources ->
                resources.ownSource("source")
                resources.ownTarget("target")
                resources.ownTemporaryFile("temporary")
                throw IOException("encode failed")
            }
        }

        assertEquals(listOf("temporary"), deleted)
        assertEquals(listOf("target", "source"), recycled)
    }

    @Test
    fun `successful processing transfers only output ownership`() {
        val recycled = mutableListOf<String>()
        val deleted = mutableListOf<String>()

        val output = resourceScope(recycled, deleted).use { resources ->
            resources.ownSource("source")
            resources.ownTarget("target")
            resources.releaseTemporaryFile(resources.ownTemporaryFile("output"))
        }

        assertEquals("output", output)
        assertEquals(emptyList<String>(), deleted)
        assertEquals(listOf("target", "source"), recycled)
    }

    private fun resourceScope(recycled: MutableList<String>, deleted: MutableList<String>) =
        ProcessingResourceScope<String, String>({ recycled += it }, { deleted += it })
}

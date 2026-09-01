package com.photocoach.app.camera

import androidx.exifinterface.media.ExifInterface
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PublishedAssetVerifierTest {
    @Test fun `asset facts reject empty unreadable and unsupported orientation`() {
        assertThrows(IllegalArgumentException::class.java) {
            AssetIntegrityValidator.validate(0, 100, 100, ExifInterface.ORIENTATION_NORMAL)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AssetIntegrityValidator.validate(100, 0, 100, ExifInterface.ORIENTATION_NORMAL)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AssetIntegrityValidator.validate(100, 100, 100, ExifInterface.ORIENTATION_FLIP_HORIZONTAL)
        }
        AssetIntegrityValidator.validate(100, 100, 200, ExifInterface.ORIENTATION_UNDEFINED)
        AssetIntegrityValidator.validate(100, 100, 200, ExifInterface.ORIENTATION_ROTATE_270)
    }

    @Test fun `motion directory requires one primary one motion and real length`() {
        val valid = """
            GCamera:MotionPhoto="1"
            Item:Semantic="Primary"
            Item:Semantic="MotionPhoto" Item:Length="120"
        """.trimIndent().toByteArray()
        AssetIntegrityValidator.validateMotionHeader(valid, 1_000)
        assertThrows(IllegalArgumentException::class.java) {
            AssetIntegrityValidator.validateMotionHeader(valid + valid, 2_000)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AssetIntegrityValidator.validateMotionHeader(
                "GCamera:MotionPhoto=\"1\" Item:Semantic=\"Primary\" Item:Semantic=\"MotionPhoto\" Item:Length=\"9999\"".toByteArray(),
                1_000,
            )
        }
    }
}

package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AssetStageKeyTest {
    @Test
    fun `original and derivative verification stages do not collide`() {
        val stages = setOf(assetStageKey(PublishedAssetKind.ORIGINAL, AssetPublishStage.VERIFY_PENDING))

        assertTrue(stages.containsAssetStage(PublishedAssetKind.ORIGINAL, AssetPublishStage.VERIFY_PENDING))
        assertFalse(stages.containsAssetStage(PublishedAssetKind.DERIVATIVE, AssetPublishStage.VERIFY_PENDING))
    }

    @Test
    fun `legacy unscoped stages are accepted only for original assets`() {
        val stages = setOf(AssetPublishStage.VERIFY_PUBLISHED.name)

        assertTrue(stages.containsAssetStage(PublishedAssetKind.ORIGINAL, AssetPublishStage.VERIFY_PUBLISHED))
        assertFalse(stages.containsAssetStage(PublishedAssetKind.DERIVATIVE, AssetPublishStage.VERIFY_PUBLISHED))
    }

    @Test
    fun `clearing derivative stages preserves original evidence`() {
        val original = assetStageKey(PublishedAssetKind.ORIGINAL, AssetPublishStage.VERIFY_PUBLISHED)
        val derivative = assetStageKey(PublishedAssetKind.DERIVATIVE, AssetPublishStage.VERIFY_PENDING)

        val remaining = setOf(original, derivative).withoutAssetStages(PublishedAssetKind.DERIVATIVE)

        assertTrue(original in remaining)
        assertFalse(derivative in remaining)
    }
}

package com.photocoach.app.creative

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreativeStyleTest {
    @Test
    fun `twelve approved styles are defined with original first`() {
        assertEquals(12, CreativeStyle.entries.size)
        assertEquals(
            listOf("原图", "自然人像", "柔光人像", "清冷人像", "清透旅行", "森林清新", "日落暖金", "美食暖色", "都市冷调", "夜色霓虹", "纪实黑白", "高反差黑白"),
            CreativeStyle.entries.map(CreativeStyle::label),
        )
        assertEquals(CreativeStyle.ORIGINAL, CreativeStyle.entries.first())
    }

    @Test
    fun `preview export and edit matrix is deterministic for every style`() {
        assertTrue(CreativeColorMatrix.isIdentity(CreativeColorMatrix.forSelection(CreativeStyle.ORIGINAL)))
        CreativeStyle.entries.drop(1).forEach { style ->
            val edit = EditAdjustment(0.1f, 0.08f, 0.12f, -0.1f, 0.06f, 0.1f, 0.75f)
            val first = CreativeColorMatrix.forSelection(style, edit)
            val second = CreativeColorMatrix.forSelection(style, edit)
            assertFalse(CreativeColorMatrix.isIdentity(first), style.label)
            assertArrayEquals(first, second, 0f)
        }
    }

    @Test
    fun `zero style strength returns to original while each explicit edit remains effective`() {
        assertTrue(
            CreativeColorMatrix.isIdentity(
                CreativeColorMatrix.forSelection(CreativeStyle.SUNSET_GOLD, EditAdjustment(styleStrength = 0f)),
            ),
        )
        val edits = listOf(
            EditAdjustment(exposureStops = 0.2f, styleStrength = 0f),
            EditAdjustment(contrast = 0.2f, styleStrength = 0f),
            EditAdjustment(saturation = 0.2f, styleStrength = 0f),
            EditAdjustment(temperature = 0.2f, styleStrength = 0f),
            EditAdjustment(tint = 0.2f, styleStrength = 0f),
            EditAdjustment(fade = 0.2f, styleStrength = 0f),
        )
        edits.forEach { edit ->
            assertFalse(CreativeColorMatrix.isIdentity(CreativeColorMatrix.forSelection(CreativeStyle.ORIGINAL, edit)))
        }
    }
}

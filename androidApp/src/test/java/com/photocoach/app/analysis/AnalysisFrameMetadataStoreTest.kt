package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AnalysisFrameMetadataStoreTest {
    @Test fun nextFrameCannotOverwriteTheMetadataOfADelayedResult() {
        val store=AnalysisFrameMetadataStore<String>()
        store.put(10,"rotation-90"); store.put(20,"rotation-180")
        assertEquals("rotation-90",store.take(10)); assertEquals("rotation-180",store.take(20))
        assertNull(store.take(10))
    }
    @Test fun onlyBoundedMetadataSurvivesAndCloseClearsIt() {
        val store=AnalysisFrameMetadataStore<Int>(2)
        repeat(10) { store.put(it.toLong(),it) }
        assertNull(store.take(0)); assertNull(store.take(7)); assertEquals(8,store.take(8))
        store.clear(); assertNull(store.take(9))
    }
}

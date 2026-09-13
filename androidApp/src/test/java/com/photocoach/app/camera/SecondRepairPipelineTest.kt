package com.photocoach.app.camera

import java.io.File
import java.util.concurrent.Executor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class SecondRepairPipelineTest {
    private class Queue : Executor {
        val jobs = ArrayDeque<Runnable>()
        override fun execute(command: Runnable) { jobs.add(command) }
        fun next() = jobs.removeFirst().run()
    }
    @TempDir lateinit var root: File
    @Test fun suspendedEffectsDoNotBlockNextOriginalOrMixTransactionResults() {
        val originals = Queue(); val effects = Queue(); val main = Queue()
        val pipeline = OriginalFirstSavePipeline(originals, effects, main)
        val published = mutableListOf<String>(); val released = mutableListOf<String>(); val results = mutableListOf<String>()
        fun submit(id: String, failure: Boolean) = pipeline.run(
            publishOriginal = { published += id }, originalReady = { released += id },
            finish = { if (failure) error("failed-$id") else id },
            completed = { results += it.getOrElse { "failed-$id" } })
        submit("first", true); originals.next(); main.next()
        assertEquals(listOf("first"), released)
        submit("second", false); originals.next(); main.next()
        assertEquals(listOf("first", "second"), published)
        assertTrue(results.isEmpty()) // both originals completed before executing any effects
        effects.next(); main.next(); effects.next(); main.next()
        assertEquals(listOf("failed-first", "second"), results)
    }
    @Test fun timeoutScheduledCallbackNotifiesFailureThenRejectsLateCallbacks() {
        val state = LockConfirmation().apply { reset(true, true) }
        val token = state.begin(true)
        val scheduled = mutableListOf<() -> Unit>(); val notifications = mutableListOf<CameraLockState>()
        scheduleLockTimeout(state, token, 3000, { _, callback -> scheduled += callback }, notifications::add)
        scheduled.single().invoke()
        assertEquals(listOf(CameraLockState(LockStatus.FAILED, LockStatus.FAILED)), notifications)
        state.focus(token, true); state.exposureApplied(token, true, 0); state.result(token, 1, true, true, true)
        assertEquals(notifications.single(), state.state)
        val newer = state.begin(false)
        scheduled.single().invoke()
        assertEquals(1, notifications.size)
        assertEquals(newer, state.generation)
    }
    @Test fun concurrentReservationsAndRetainedExportsShareQuotaWithoutDeletingSources() {
        val limits = PendingStorageLimits(100, 20, 50, 60, 10)
        val first = PendingSourceStore(root, limits); val second = PendingSourceStore(root, limits)
        val old = File(root, "failed-export.jpg").apply { writeBytes(ByteArray(31)) }
        first.reserve(creative = true, freeBytes = 1000).use {
            assertThrows(java.io.IOException::class.java) { second.reserve(creative = false, freeBytes = 1000) }
        }
        second.reserve(creative = false, freeBytes = 1000).close()
        assertEquals(31L, old.length())
        old.appendBytes(ByteArray(30))
        assertThrows(java.io.IOException::class.java) { second.generate { error("must not generate") } }
        assertEquals(61L, old.length())
    }
}

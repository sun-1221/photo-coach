package com.photocoach.app.camera

import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SaveTransactionRegistryTest {
    @Test
    fun `recreated owner cannot recover a save still running after executor shutdown`() {
        val key = UUID.randomUUID().toString()
        val oldSave = requireNotNull(SaveTransactionRegistry.tryAcquire(key))
        val executor = Executors.newSingleThreadExecutor()
        val entered = CountDownLatch(1)
        val finish = CountDownLatch(1)
        try {
            val work = executor.submit {
                oldSave.use {
                    entered.countDown()
                    check(finish.await(5, TimeUnit.SECONDS))
                }
            }
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            executor.shutdown()
            assertNull(SaveTransactionRegistry.tryAcquire(key))
            finish.countDown()
            work.get(5, TimeUnit.SECONDS)
            val recovery = requireNotNull(SaveTransactionRegistry.tryAcquire(key))
            recovery.use {
                oldSave.close() // A late old callback must not release the new owner.
                assertNull(SaveTransactionRegistry.tryAcquire(key))
            }
        } finally {
            finish.countDown()
            executor.shutdownNow()
            oldSave.close()
        }
    }

    @Test
    fun `different captures can own transactions independently`() {
        val first = requireNotNull(SaveTransactionRegistry.tryAcquire(UUID.randomUUID().toString()))
        first.use {
            requireNotNull(SaveTransactionRegistry.tryAcquire(UUID.randomUUID().toString())).use { }
        }
    }
}

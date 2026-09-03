package com.photocoach.app.analysis

import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ClosingAnalyzerExecutorTest {
    @Test fun openExecutorUsesWorkerAndLateCompletionStillRunsAfterClose() {
        var delegated = 0
        var completed = 0
        val executor = ClosingAnalyzerExecutor(Executor { delegated++; it.run() })
        executor.execute { completed++ }
        executor.close()
        executor.execute { completed++ }
        assertEquals(1,delegated)
        assertEquals(2,completed)
    }

    @Test fun shutdownRacingSubmissionCompletesCleanupExactlyOnce() {
        lateinit var executor: ClosingAnalyzerExecutor
        var completed = 0
        executor = ClosingAnalyzerExecutor(Executor {
            executor.close()
            throw RejectedExecutionException("worker shutdown")
        })
        executor.execute { completed++ }
        assertEquals(1,completed)
    }

    @Test fun unexpectedWorkerFailureIsNotHidden() {
        val executor = ClosingAnalyzerExecutor(Executor { throw RejectedExecutionException("unexpected") })
        assertThrows(RejectedExecutionException::class.java) { executor.execute {} }
    }
}

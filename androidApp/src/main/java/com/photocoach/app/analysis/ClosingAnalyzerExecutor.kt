package com.photocoach.app.analysis

import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException

/** ML Kit must finish its task chain and close the image even after the camera worker shuts down. */
internal class ClosingAnalyzerExecutor(private val delegate: Executor) : Executor {
    @Volatile private var closed = false

    fun close() { closed = true }

    override fun execute(command: Runnable) {
        if (closed) {
            command.run()
            return
        }
        try { delegate.execute(command) }
        catch (error: RejectedExecutionException) {
            // close() can race the submission; do not swallow unrelated executor failures.
            if (closed) command.run() else throw error
        }
    }
}

package com.photocoach.app.camera

import java.util.concurrent.Executor

/** Original publication has its own queue; a suspended effect never delays another original. */
internal class OriginalFirstSavePipeline(private val originals: Executor, private val effects: Executor, private val main: Executor) {
    fun <T> run(publishOriginal: () -> Unit, originalReady: () -> Unit, finish: () -> T,
        completed: (Result<T>) -> Unit) {
        originals.execute {
            val published = runCatching(publishOriginal)
            main.execute {
                published.fold(onSuccess = {
                    originalReady()
                    effects.execute { val result = runCatching(finish); main.execute { completed(result) } }
                }, onFailure = { completed(Result.failure(it)) })
            }
        }
    }
}

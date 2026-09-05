package com.photocoach.app.camera

import java.io.Closeable

/** Process-wide ownership, including gaps between CameraX callbacks and queued disk work. */
internal object SaveTransactionRegistry {
    private val owners = mutableMapOf<String, Lease>()

    @Synchronized
    fun tryAcquire(key: String): Lease? {
        if (key in owners) return null
        return Lease(key).also { owners[key] = it }
    }

    internal class Lease internal constructor(private val key: String) : Closeable {
        override fun close() = synchronized(SaveTransactionRegistry) {
            if (owners[key] === this) owners.remove(key)
            Unit
        }
    }
}

package com.photocoach.app.camera

/** Deferred disk transactions never take ownership of the current capture's retry button. */
internal class RecoveryInbox {
    var records: List<SaveJournal> = emptyList(); private set
    var open = false; private set
    var retryingKey: String? = null; private set
    fun refresh(records: List<SaveJournal>) { this.records = records.toList(); retryingKey = null }
    fun show(open: Boolean) { this.open = open }
    fun beginRetry(key: String): Boolean {
        if (retryingKey != null || records.none { it.key == key }) return false
        retryingKey = key
        return true
    }
}

package com.photocoach.app.camera

import java.io.File

internal data class GeneratedExport(val file: File, val downsampled: Boolean = false, val warning: String? = null)
internal data class ExportResult(val uri: String, val downsampled: Boolean, val warning: String?)

/** One implementation for online creation, explicit retries and process recovery. */
internal class ExportTransactionRunner(
    private val store: SaveJournalStore,
    private val generate: (SaveJournal) -> GeneratedExport,
    private val publish: (SaveJournal, File, (String) -> Unit) -> String,
    private val verify: (SaveJournal, String) -> Unit,
    private val write: (SaveJournal) -> Unit = { store.write(it); Unit },
    private val cleanup: (File) -> Unit = { it.delete(); Unit },
) {
    fun create(request: SaveJournal): ExportResult = owned(request) {
        check(store.read(request) == null) { "导出标识已存在，请重试原事务" }
        write(request)
        run(request)
    }

    fun retry(identity: SaveJournal): ExportResult = owned(identity) { run(identity) }

    private fun <T> owned(record: SaveJournal, action: () -> T): T {
        val lease = SaveTransactionRegistry.tryAcquire(store.transactionKey(record))
            ?: error("此副本正在保存，请稍后重试")
        return lease.use { action() }
    }

    private fun run(identity: SaveJournal): ExportResult {
        var record = store.read(identity) ?: error("导出记录不存在；不会重新建立旧事务")
        try {
            val existingUri = record.derivativeUri
            if (existingUri != null) {
                verify(record, existingUri)
            } else {
                check(SaveStage.COMPLETE.name !in record.completedStages) { "完成记录缺少 URI，需检查" }
                var file = record.derivativePath?.let(::File)?.takeIf { it.isFile && it.length() > 0 }
                if (file == null) {
                    val generated = generate(record)
                    file = generated.file
                    record = record.copy(derivativePath = file.absolutePath, effectWasDownsampled = generated.downsampled,
                        exportWarning = generated.warning, completedStages = record.completedStages + SaveStage.DERIVATIVE_GENERATE.name,
                        failedStage = SaveStage.DERIVATIVE_PUBLISH.name)
                    write(record)
                }
                val uri = publish(record, file) { pending ->
                    record = record.copy(derivativePendingUri = pending)
                    write(record)
                }
                record = record.copy(derivativeUri = uri)
            }
            record = record.copy(completedStages = record.completedStages + SaveStage.COMPLETE.name, failedStage = null, error = null)
            write(record)
            // Completion is durable before cleanup. Cleanup failure cannot cause another publication.
            record.derivativePath?.let(::File)?.let { runCatching { cleanup(it) } }
            return ExportResult(requireNotNull(record.derivativeUri), record.effectWasDownsampled, record.exportWarning)
        } catch (error: Throwable) {
            runCatching { write(record.copy(error = error.message ?: "另存失败")) }
            throw error
        }
    }
}

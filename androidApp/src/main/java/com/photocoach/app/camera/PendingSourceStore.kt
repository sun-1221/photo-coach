package com.photocoach.app.camera

import java.io.File
import java.io.IOException

/** Configurable engineering bounds; target-device storage calibration remains outstanding. */
data class PendingStorageLimits(
    val quotaBytes: Long = 512L * 1024 * 1024,
    val stillPeakBytes: Long = 64L * 1024 * 1024,
    val creativePeakBytes: Long = 160L * 1024 * 1024,
    val livePeakBytes: Long = 192L * 1024 * 1024,
    val reserveBytes: Long = 32L * 1024 * 1024,
)

internal class PendingSourceStore(val directory: File, private val limits: PendingStorageLimits = PendingStorageLimits()) {
    private val key get() = directory.canonicalPath
    fun checkSpace(freeBytes: Long, creative: Boolean, live: Boolean) = synchronized(reservations) {
        val peak = if (live) limits.livePeakBytes else if (creative) limits.creativePeakBytes else limits.stillPeakBytes
        val used = directory.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        val held = reservations[key] ?: 0L
        if (used + held > limits.quotaBytes - peak) throw IOException("待保存照片已达到暂存配额，请先重试保存或明确放弃旧照片")
        if (freeBytes < peak + held + limits.reserveBytes) throw IOException("存储空间不足，无法安全保留原片和处理临时文件")
    }
    fun reserve(creative: Boolean, live: Boolean = false, freeBytes: Long? = null): java.io.Closeable = synchronized(reservations) {
        check(directory.isDirectory || directory.mkdirs()) { "无法建立持久暂存目录" }
        checkSpace(freeBytes ?: directory.usableSpace, creative, live)
        val peak = if (live) limits.livePeakBytes else if (creative) limits.creativePeakBytes else limits.stillPeakBytes
        val reservationKey = key
        reservations[reservationKey] = (reservations[reservationKey] ?: 0L) + peak
        var closed = false
        java.io.Closeable { synchronized(reservations) {
            if (!closed) { closed = true; reservations[reservationKey] = (reservations[reservationKey] ?: 0L) - peak }
        } }
    }
    fun <T> generate(action: () -> T): T = reserve(creative = true).use { action() }
    fun create(prefix: String): File {
        check(directory.isDirectory || directory.mkdirs()) { "无法建立持久暂存目录" }
        return File.createTempFile(prefix, ".jpg", directory)
    }
    fun preserveLegacy(source: File): File {
        if (!source.isFile || source.canonicalFile.toPath().startsWith(directory.canonicalFile.toPath())) return source
        check(directory.isDirectory || directory.mkdirs()) { "无法建立持久暂存目录" }
        return reserve(creative = false).use {
            val target = create("legacy-")
            try {
                source.inputStream().use { input -> target.outputStream().use { out -> input.copyTo(out); out.fd.sync() } }
                check(target.length() == source.length()) { "暂存迁移校验失败" }
                target
            } catch (error: Throwable) { target.delete(); throw error }
        }
    }
    companion object { private val reservations = mutableMapOf<String, Long>() }
}

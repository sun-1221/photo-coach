package com.photocoach.app.analysis

/** Metadata only. MlKitAnalyzer closes ImageProxy before scheduling its result consumer. */
internal class AnalysisFrameMetadataStore<T>(private val capacity: Int = 3) {
    init { require(capacity > 0) }
    private val frames = linkedMapOf<Long, T>()
    @Synchronized fun put(timestampNs: Long, value: T) {
        frames[timestampNs] = value
        while (frames.size > capacity) frames.remove(frames.keys.first())
    }
    @Synchronized fun take(timestampNs: Long): T? = frames.remove(timestampNs)
    @Synchronized fun clear() = frames.clear()
}

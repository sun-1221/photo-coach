package com.photocoach.app.camera

import java.io.IOException

/** Replacement is permitted only after MediaStore has positively confirmed absence. */
internal fun retireAsset(exists: () -> Boolean, delete: () -> Int) {
    if (!exists()) return
    if (delete() != 1 || exists()) throw IOException("无法确认失败照片已清理，保留原事务重试")
}

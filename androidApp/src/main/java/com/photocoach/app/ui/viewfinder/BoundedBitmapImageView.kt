package com.photocoach.app.ui.viewfinder

import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.photocoach.app.creative.BoundedImageDecoder
import java.util.concurrent.Executors

internal class BoundedBitmapImageController(
    private val imageView: ImageView,
) {
    private val decoder = BoundedImageDecoder(imageView.context.contentResolver)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var request: ImageRequest? = null
    private var ownedBitmap: Bitmap? = null
    private var loading = false
    private var generation = 0L
    private var lastError: String? = null
    private var errorCallback: ((String?) -> Unit)? = null

    fun loadBounded(uri: Uri, maximumPixels: Long, onError: (String?) -> Unit) {
        val next = ImageRequest(uri, maximumPixels)
        errorCallback = onError
        if (next == request && (loading || ownedBitmap != null || lastError != null)) {
            onError(lastError)
            return
        }

        request = next
        generation++
        val loadGeneration = generation
        loading = true
        lastError = null
        onError(null)
        clearOwnedBitmap()

        decodeExecutor.execute {
            val result = runCatching { decoder.decode(uri, maximumPixels) }
            mainHandler.post {
                val decoded = result.getOrNull()
                if (loadGeneration != generation || request != next) {
                    decoded?.bitmap?.recycle()
                    return@post
                }
                loading = false
                result.onSuccess { image ->
                    ownedBitmap = image.bitmap
                    imageView.setImageBitmap(image.bitmap)
                    lastError = null
                    errorCallback?.invoke(null)
                }.onFailure { error ->
                    imageView.setImageDrawable(null)
                    lastError = error.message ?: "照片加载失败"
                    errorCallback?.invoke(lastError)
                }
            }
        }
    }

    fun releaseImage() {
        generation++
        request = null
        loading = false
        lastError = null
        errorCallback = null
        clearOwnedBitmap()
    }

    private fun clearOwnedBitmap() {
        imageView.setImageDrawable(null)
        ownedBitmap?.recycle()
        ownedBitmap = null
    }

    private data class ImageRequest(val uri: Uri, val maximumPixels: Long)

    private companion object {
        val decodeExecutor = Executors.newFixedThreadPool(2) { runnable ->
            Thread(runnable, "bounded-local-image").apply { isDaemon = true }
        }
    }
}

package com.photocoach.app.camera

import androidx.camera.core.Camera
import androidx.camera.core.CameraEffect
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SessionConfig
import androidx.camera.core.UseCase
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.lifecycle.LifecycleOwner

/** Owns every use case submitted by one logical camera session, including failed attempts. */
internal class CameraSessionCoordinator(
    private val provider: ProcessCameraProvider,
    private val owner: LifecycleOwner,
    private val preview: Preview,
    private val analysis: ImageAnalysis,
    private val capture: ImageCapture,
    private val viewPort: ViewPort,
    private val effect: () -> CameraEffect?,
) {
    private val ownedUseCases = mutableSetOf<UseCase>()

    fun isSupported(selector: CameraSelector, video: VideoCapture<Recorder>,videoEffect:CameraEffect?=null): Boolean = runCatching {
        provider.getCameraInfo(selector).isSessionConfigSupported(
            SessionConfig.Builder(listOf(preview, analysis, capture, video))
                .setViewPort(viewPort)
                .apply {videoEffect?.let {addEffect(it)};effect()?.let {addEffect(it)}}
                .build(),
        )
    }.getOrDefault(true)

    fun bind(selector: CameraSelector, video: VideoCapture<Recorder>?,videoEffect:CameraEffect?=null): Camera {
        val group = UseCaseGroup.Builder()
            .setViewPort(viewPort)
            .addUseCase(preview)
            .addUseCase(analysis)
            .addUseCase(capture)
            .apply { if (video != null) addUseCase(video) }
            .apply { effect()?.let(::addEffect) }
            .apply { videoEffect?.let(::addEffect) }
            .build()
        // Register before binding: partially failed attempts still belong to this coordinator.
        ownedUseCases.addAll(group.useCases)
        return provider.bindToLifecycle(owner, selector, group)
    }

    fun unbind() {
        if (ownedUseCases.isEmpty()) return
        provider.unbind(*ownedUseCases.toTypedArray())
        ownedUseCases.clear()
    }
}

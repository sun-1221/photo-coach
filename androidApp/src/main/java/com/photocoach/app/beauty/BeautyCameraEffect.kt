package com.photocoach.app.beauty

import android.graphics.SurfaceTexture
import android.opengl.EGLSurface
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import androidx.camera.core.CameraEffect
import androidx.camera.core.ProcessingException
import androidx.camera.core.SurfaceOutput
import androidx.camera.core.SurfaceProcessor
import androidx.camera.core.SurfaceRequest
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class BeautyCameraEffect(private val processor: BeautySurfaceProcessor) : CameraEffect(
    PREVIEW, processor.executor, processor, { processor.reportFailure(it) },
), AutoCloseable {
    constructor(store:BeautyFaceStore,preset:()->BeautyPreset,enabled:()->Boolean,onFailure:(Throwable)->Unit) :
        this(BeautySurfaceProcessor(store,preset,enabled,onFailure))
    override fun close() = processor.close()
}

class BeautySurfaceProcessor internal constructor(
    private val store:BeautyFaceStore, private val preset:()->BeautyPreset,
    private val enabled:()->Boolean, private val onFailure:(Throwable)->Unit,
) : SurfaceProcessor, AutoCloseable {
    private val thread=HandlerThread("beauty-gl").apply { start() }
    private val handler=Handler(thread.looper)
    val executor=Executor { runnable -> if(!handler.post(runnable)) runnable.run() }
    private val closed=AtomicBoolean(false)
    private val failed=AtomicBoolean(false)
    private var gl:BeautyGlRenderer?=null
    private val inputs=mutableMapOf<SurfaceTexture,Input>()
    private val outputs=mutableMapOf<SurfaceOutput,EGLSurface>()
    private var currentInput:SurfaceTexture?=null
    private val originalTransform=FloatArray(16)
    private val outputTransform=FloatArray(16)

    private fun renderer() = gl ?: BeautyGlRenderer().also { gl=it }
    internal fun reportFailure(error:Throwable) {
        if(!closed.get() && failed.compareAndSet(false,true)) onFailure(error)
    }
    override fun onInputSurface(request:SurfaceRequest) {
        if(closed.get()) { request.willNotProvideSurface(); return }
        if(failed.get()) { request.willNotProvideSurface(); throw ProcessingException() }
        var input:Input?=null
        var allocatedTexture = 0
        var allocatedSurfaceTexture: SurfaceTexture? = null
        try {
            val renderer=renderer(); renderer.makeCurrent()
            val texture=renderer.texture(external=true)
            allocatedTexture = texture
            val st=SurfaceTexture(texture)
            allocatedSurfaceTexture = st
            st.setDefaultBufferSize(request.resolution.width,request.resolution.height)
            input=Input(st,Surface(st),texture,request)
            val owned=input
            inputs[st]=owned; currentInput=st
            st.setOnFrameAvailableListener({ frame -> draw(frame) },handler)
            request.provideSurface(owned.surface,executor) {
                // Callback acknowledges CameraX ownership; queued late frames must not touch it.
                try { inputs.remove(st)?.let(::releaseInput) }
                finally {
                    if(currentInput===st) currentInput=null
                    finishClose()
                }
            }
        } catch(error:Exception) {
            if (input != null) { inputs.remove(input.texture); releaseInput(input) }
            else {
                allocatedSurfaceTexture?.release()
                if (allocatedTexture != 0) gl?.deleteTexture(allocatedTexture)
            }
            request.willNotProvideSurface(); reportFailure(error); throw ProcessingException().also { it.initCause(error) }
        }
    }
    override fun onOutputSurface(output:SurfaceOutput) {
        if(closed.get()) { output.close(); return }
        try {
            val surface=output.getSurface(executor) { closeOutput(output) }
            outputs[output]=renderer().window(surface)
        } catch(error:Exception) {
            output.close(); reportFailure(error); throw ProcessingException().also { it.initCause(error) }
        }
    }
    private fun draw(st:SurfaceTexture) {
        if(closed.get() || failed.get() || st!==currentInput || !inputs.containsKey(st)) return
        try {
            val renderer=renderer(); renderer.makeCurrent()
            st.updateTexImage(); st.getTransformMatrix(originalTransform)
            val frame=store.snapshot()
            val freshness=frame?.let { BeautyFaceStore.freshness(it.timestampNs,st.timestamp) } ?: 0f
            outputs.forEach { (output,window) ->
                output.updateTransformMatrix(outputTransform,originalTransform)
                val transform=output.sensorToBufferTransform.beautyTransform()?.inverse()?.let { inverse ->
                    frame?.sensorToAnalysis?.times(inverse)
                }
                renderer.render(inputs.getValue(st).id,true,outputTransform,window,
                    output.size.width,output.size.height,frame?.mask,transform,
                    if(enabled()) preset() else BeautyPreset.OFF,freshness)
                renderer.swap(window)
            }
        } catch(error:Exception) {
            inputs[st]?.request?.invalidate()
            reportFailure(error)
        }
    }
    private fun releaseInput(input:Input) {
        // Context loss must not prevent acknowledgement/release of the remaining CameraX surfaces.
        runCatching { input.texture.setOnFrameAvailableListener(null) }.onFailure(::reportFailure)
        runCatching { input.surface.release() }.onFailure(::reportFailure)
        runCatching { input.texture.release() }.onFailure(::reportFailure)
        runCatching { gl?.let { it.makeCurrent(); it.deleteTexture(input.id) } }.onFailure(::reportFailure)
    }
    private fun closeOutput(output:SurfaceOutput) {
        try { outputs.remove(output)?.let { gl?.destroy(it) } }
        catch (error: Exception) { reportFailure(error) }
        finally { output.close() }
    }
    override fun close() {
        if(!closed.compareAndSet(false,true)) return
        executor.execute {
            try {
                outputs.keys.toList().forEach { runCatching { closeOutput(it) } }
                inputs.keys.forEach { runCatching { it.setOnFrameAvailableListener(null) } }
            } finally { finishClose() }
        }
    }
    private fun finishClose() {
        if(closed.get() && inputs.isEmpty()) {
            try { gl?.close() }
            finally { gl=null; thread.quitSafely() }
        }
    }
    private data class Input(val texture:SurfaceTexture,val surface:Surface,val id:Int,val request:SurfaceRequest)
}

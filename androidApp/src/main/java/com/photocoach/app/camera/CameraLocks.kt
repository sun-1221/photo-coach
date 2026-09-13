package com.photocoach.app.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.os.Handler
import android.os.Looper
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import java.util.concurrent.Executor

@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
internal class CameraLocks(private val executor: Executor, private val timeoutMs: Long = 3_000L) {
    // Engineering timeout, not a calibrated device/acceptance threshold.
    private val confirmation = LockConfirmation()
    private val handler = Handler(Looper.getMainLooper())
    private var camera: Camera? = null
    @Volatile private var session = 0L
    private var lastFrame = -1L
    var timestampsRealtime = false; private set
    var onState: (CameraLockState) -> Unit = {}
    var onSensorResult:(MotionSensorSource,Long,Long)->Unit = {_,_,_->}
    @Volatile var sensorSource:MotionSensorSource?=null;private set
    val state get() = confirmation.state
    fun invalidate() { session++; camera = null; sensorSource=null; timestampsRealtime = false; confirmation.reset(false, false); onState(state) }
    fun observe(builder: Preview.Builder) {
        val binding = session
        Camera2Interop.Extender(builder).setSessionCaptureCallback(object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(s: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                val observedSource=sensorSource
                if(binding==session && observedSource!=null)result.get(CaptureResult.SENSOR_TIMESTAMP)?.let {
                    onSensorResult(observedSource,it,result.frameNumber)
                }
                executor.execute {
                    if (binding != session) return@execute
                    lastFrame = maxOf(lastFrame, result.frameNumber)
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    val locked = aeState?.let { it == CaptureResult.CONTROL_AE_STATE_LOCKED }
                    val old = state
                    confirmation.result(confirmation.generation, result.frameNumber,
                        request.get(CaptureRequest.CONTROL_AE_LOCK), result.get(CaptureResult.CONTROL_AE_LOCK), locked)
                    if (old != state) onState(state)
                }
            }
        })
    }
    fun bind(value: Camera, standard: Boolean) {
        camera = value; lastFrame = -1
        val info = runCatching { Camera2CameraInfo.from(value.cameraInfo) }.getOrNull()
        sensorSource=info?.let {MotionSensorSource(it.cameraId,session+1)}
        timestampsRealtime = runCatching { info?.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE) ==
            CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE_REALTIME }.getOrDefault(false)
        val af = runCatching { info?.getCameraCharacteristic(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
            ?.any { it == CaptureRequest.CONTROL_AF_MODE_AUTO } == true }.getOrDefault(false)
        val ae = standard && runCatching { info?.getCameraCharacteristic(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE) == true }.getOrDefault(false)
        confirmation.reset(af, ae); onState(state)
    }
    fun request(action: FocusMeteringAction?, lock: Boolean, onFocus: (Boolean) -> Unit = {}) {
        val active = camera ?: return
        val token = confirmation.begin(lock)
        onState(state)
        if (state.af != LockStatus.UNSUPPORTED) {
            runCatching {
                if (action == null) active.cameraControl.cancelFocusAndMetering().also { future ->
                    future.addListener({
                        if (token != confirmation.generation) return@addListener
                        confirmation.focus(token, runCatching { future.get(); true }.getOrDefault(false)); onState(state)
                    }, executor)
                } else active.cameraControl.startFocusAndMetering(action).also { future ->
                    future.addListener({
                        if (token != confirmation.generation) return@addListener
                        val success = runCatching { future.get().isFocusSuccessful }.getOrDefault(false)
                        confirmation.focus(token, success); onFocus(success); onState(state)
                    }, executor)
                }
            }.onFailure { confirmation.focus(token, false); onFocus(false); onState(state) }
        } else if (action != null) onFocus(false)
        if (state.ae != LockStatus.UNSUPPORTED) {
            runCatching {
                val control = Camera2CameraControl.from(active.cameraControl)
                val future = control.addCaptureRequestOptions(CaptureRequestOptions.Builder()
                    .setCaptureRequestOption(CaptureRequest.CONTROL_AE_LOCK, lock).build())
                future.addListener({
                    if (token != confirmation.generation) return@addListener
                    confirmation.exposureApplied(token, runCatching { future.get(); true }.getOrDefault(false), lastFrame)
                    onState(state)
                }, executor)
            }.onFailure { confirmation.exposureApplied(token, false, lastFrame); onState(state) }
        }
        scheduleLockTimeout(confirmation, token, timeoutMs,
            schedule = { delay, callback -> handler.postDelayed({ callback() }, delay) },
            notify = { onState(it) })
    }
}

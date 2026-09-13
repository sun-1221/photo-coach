package com.photocoach.app.camera

import android.os.Handler
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraLockTimeoutInstrumentedTest {
    @Test fun realMainHandlerDeliversTimeoutFailureAndOldResultCannotConfirm() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val confirmation = LockConfirmation()
        val completed = CountDownLatch(1)
        val notifications = mutableListOf<CameraLockState>()
        var token = 0L
        instrumentation.runOnMainSync {
            confirmation.reset(true, true); token = confirmation.begin(true)
            val handler = Handler(Looper.getMainLooper())
            scheduleLockTimeout(confirmation, token, 25,
                { delay, callback -> handler.postDelayed({ callback() }, delay) },
                { state -> notifications += state; completed.countDown() })
        }
        assertTrue(completed.await(2, TimeUnit.SECONDS))
        instrumentation.runOnMainSync {
            assertEquals(CameraLockState(LockStatus.FAILED, LockStatus.FAILED), notifications.single())
            confirmation.focus(token, true)
            confirmation.exposureApplied(token, true, 0)
            confirmation.result(token, 1, true, true, true)
            assertEquals(notifications.single(), confirmation.state)
        }
    }
}

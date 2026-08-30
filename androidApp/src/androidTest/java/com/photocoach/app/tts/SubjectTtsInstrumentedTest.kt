package com.photocoach.app.tts

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.photocoach.coach.Audience
import com.photocoach.coach.Channel
import com.photocoach.coach.Cue
import com.photocoach.coach.CueId
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GuidanceTtsInstrumentedTest {
    private var guidanceTts: GuidanceTts? = null

    @After
    fun tearDown() {
        guidanceTts?.shutdown()
    }

    @Test
    fun installedTtsEngineIsVisibleToTheAppPackage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val engines = context.packageManager.queryIntentServices(
            Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE),
            0,
        )

        assertTrue("No installed TTS service is visible to the app", engines.isNotEmpty())
    }

    @Test
    fun chineseShooterCueCompletesOnTheEmulatorTtsEngine() {
        val completion = CountDownLatch(1)
        val outcome = AtomicReference<String>()
        guidanceTts = GuidanceTts(InstrumentationRegistry.getInstrumentation().targetContext)

        guidanceTts?.speak(
            cue = Cue(
                id = CueId.MOVE_CLOSER,
                text = "走近一步",
                audience = Audience.SHOOTER,
                channel = Channel.COMPOSITION,
            ),
            muted = false,
            onUnavailable = {
                outcome.compareAndSet(null, "unavailable")
                completion.countDown()
            },
            onFailure = { outcome.compareAndSet(null, "failure") },
            onFinished = {
                outcome.set("finished")
                completion.countDown()
            },
        )

        assertTrue("TTS did not reach a terminal callback", completion.await(20, TimeUnit.SECONDS))
        assertEquals("finished", outcome.get())
    }
}

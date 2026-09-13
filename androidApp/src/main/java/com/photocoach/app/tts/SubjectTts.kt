package com.photocoach.app.tts

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.photocoach.coach.Cue
import java.util.Locale

class GuidanceTts(context: Context) {
    private val main = android.os.Handler(android.os.Looper.getMainLooper())
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val completion = SpeechCompletionGate()
    private val readiness = TtsReadinessGate<PendingSpeech>()
    private var tts: TextToSpeech? = null
    internal val isSpeaking: Boolean get() = tts?.isSpeaking == true
    internal var submittedUtterances: Long = 0
        private set
    /** Counts only a SUCCESS stop request issued while the real engine was speaking. */
    internal var stoppedWhileSpeaking: Long = 0
        private set

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            val engine = tts
            val languageStatus = if (status == TextToSpeech.SUCCESS && engine != null) {
                configureChineseVoice(engine)
            } else {
                TextToSpeech.LANG_NOT_SUPPORTED
            }
            val available = languageStatus != TextToSpeech.LANG_MISSING_DATA &&
                languageStatus != TextToSpeech.LANG_NOT_SUPPORTED
            if (available && engine != null) {
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        // enqueue owns the ID; a late old onStart cannot replace it.
                    }

                    override fun onDone(utteranceId: String?) {
                        complete(utteranceId, failed = false)

                    }

                    @Deprecated("Deprecated by Android")
                    override fun onError(utteranceId: String?) {
                        handleError(utteranceId)
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        handleError(utteranceId)
                    }

                    override fun onStop(utteranceId: String?, interrupted: Boolean) {
                        complete(utteranceId, failed = true)

                    }
                })
                readiness.markReady()?.let(::enqueue)
            } else {
                readiness.markFailed()?.reportUnavailable()
            }
        }
    }

    fun speak(
        cue: Cue?,
        muted: Boolean,
        onUnavailable: () -> Unit,
        onFailure: () -> Unit,
        onFinished: () -> Unit,
    ) {
        if (muted) {
            stop()
            onUnavailable()
            return
        }
        if (cue == null) return
        if (isStreamMuted()) {
            onFailure()
            onUnavailable()
            return
        }
        val request = PendingSpeech(cue, SpeechCallbacks(onFinished, onFailure, onUnavailable))
        when (readiness.offer(request)) {
            TtsOfferResult.PLAY_NOW -> enqueue(request)
            TtsOfferResult.QUEUED -> Unit
            TtsOfferResult.UNAVAILABLE -> request.reportUnavailable()
        }
    }

    private fun enqueue(request: PendingSpeech) {
        val engine = tts ?: run {
            request.reportUnavailable()
            return
        }
        cancelActiveSpeech()
        val utteranceId = "${request.cue.id.name}-${System.nanoTime()}"
        completion.begin(utteranceId) { failed ->
            if (failed) request.reportUnavailable() else request.callbacks.onFinished()
        }
        val speechParams = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        val result = engine.speak(request.cue.text, TextToSpeech.QUEUE_FLUSH, speechParams, utteranceId)
        if (result == TextToSpeech.SUCCESS) submittedUtterances++
        if (result != TextToSpeech.SUCCESS) {
            completion.cancel()
            request.reportUnavailable()
        }
    }

    private fun handleError(utteranceId: String?) { complete(utteranceId, failed = true) }

    private fun complete(id: String?, failed: Boolean) {
        main.post {
            completion.complete(id, failed)
        }
    }
    private fun configureChineseVoice(engine: TextToSpeech): Int {
        val languageStatus = engine.setLanguage(Locale.SIMPLIFIED_CHINESE)
        if (languageStatus == TextToSpeech.LANG_MISSING_DATA ||
            languageStatus == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            return languageStatus
        }

        // Keep the coaching path offline when the engine exposes a local Chinese voice.
        // If the voice list is absent, the selected voice must still explicitly be offline.
        val voice = preferredOfflineChineseVoice(engine)
        if (voice != null) {
            if (engine.setVoice(voice) != TextToSpeech.SUCCESS) return TextToSpeech.LANG_NOT_SUPPORTED
        } else if (runCatching { engine.voice?.isNetworkConnectionRequired != false }.getOrDefault(true)) {
            return TextToSpeech.LANG_MISSING_DATA
        }
        engine.setSpeechRate(CHINESE_SPEECH_RATE)
        engine.setPitch(CHINESE_PITCH)
        return languageStatus
    }

    private fun preferredOfflineChineseVoice(engine: TextToSpeech): Voice? = runCatching {
        engine.voices.orEmpty()
            .asSequence()
            .filter { it.locale.language.equals(Locale.CHINESE.language, ignoreCase = true) }
            .filterNot(Voice::isNetworkConnectionRequired)
            .sortedWith(
                compareByDescending<Voice> { it.locale.country.equals(Locale.CHINA.country, ignoreCase = true) }
                    .thenByDescending(Voice::getQuality)
                    .thenBy(Voice::getLatency)
                    .thenBy(Voice::getName),
            )
            .firstOrNull()
    }.getOrNull()

    fun isStreamMuted(): Boolean = audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0

    fun stop() {
        readiness.cancelPending()
        cancelActiveSpeech()
    }

    private fun cancelActiveSpeech() {
        val wasSpeaking = tts?.isSpeaking == true
        completion.cancel()
        val result = tts?.stop()
        if (wasSpeaking && result == TextToSpeech.SUCCESS) stoppedWhileSpeaking++
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }

    private data class SpeechCallbacks(
        val onFinished: () -> Unit,
        val onFailure: () -> Unit,
        val onUnavailable: () -> Unit,
    ) {
        fun reportUnavailable() {
            onFailure()
            onUnavailable()
        }
    }

    private data class PendingSpeech(
        val cue: Cue,
        val callbacks: SpeechCallbacks,
    ) {
        fun reportUnavailable() = callbacks.reportUnavailable()
    }

    private companion object {
        const val CHINESE_SPEECH_RATE = 0.92f
        const val CHINESE_PITCH = 1.0f
    }
}

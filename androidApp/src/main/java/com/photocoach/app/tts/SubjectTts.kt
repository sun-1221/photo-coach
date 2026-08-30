package com.photocoach.app.tts

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.photocoach.coach.Cue
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class GuidanceTts(context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val callbacks = ConcurrentHashMap<String, SpeechCallbacks>()
    private val readiness = TtsReadinessGate<PendingSpeech>()
    private var tts: TextToSpeech? = null
    @Volatile
    private var activeUtteranceId: String? = null

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
                        activeUtteranceId = utteranceId
                    }

                    override fun onDone(utteranceId: String?) {
                        utteranceId?.let {
                            if (activeUtteranceId == it) activeUtteranceId = null
                            callbacks.remove(it)?.onFinished?.invoke()
                        }
                    }

                    @Deprecated("Deprecated by Android")
                    override fun onError(utteranceId: String?) {
                        handleError(utteranceId)
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        handleError(utteranceId)
                    }

                    override fun onStop(utteranceId: String?, interrupted: Boolean) {
                        utteranceId?.let {
                            if (activeUtteranceId == it) activeUtteranceId = null
                            callbacks.remove(it)?.onUnavailable?.invoke()
                        }
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
        callbacks[utteranceId] = request.callbacks
        activeUtteranceId = utteranceId
        val speechParams = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        val result = engine.speak(request.cue.text, TextToSpeech.QUEUE_FLUSH, speechParams, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            activeUtteranceId = null
            callbacks.remove(utteranceId)
            request.reportUnavailable()
        }
    }

    private fun handleError(utteranceId: String?) {
        utteranceId?.let { id ->
            if (activeUtteranceId == id) activeUtteranceId = null
            callbacks.remove(id)?.reportUnavailable()
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
        // Some OEM engines return no voice list; setLanguage remains the safe fallback.
        preferredOfflineChineseVoice(engine)?.let { voice ->
            if (engine.setVoice(voice) != TextToSpeech.SUCCESS) {
                engine.setLanguage(Locale.SIMPLIFIED_CHINESE)
            }
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
        activeUtteranceId?.let(callbacks::remove)
        activeUtteranceId = null
        tts?.stop()
    }

    fun shutdown() {
        stop()
        callbacks.clear()
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

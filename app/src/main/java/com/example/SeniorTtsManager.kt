package com.example

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.i18n.LocaleFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * =========================================================================================
 * FEATURE: Senior Mode Text-to-Speech (TTS) Engine (SeniorTtsManager)
 * =========================================================================================
 *
 * Description:
 * Accessibility voice engine converting calculated bill summaries, tips, and per-person split
 * amounts into loud, clear speech synthesis for senior and visually impaired users.
 * Supports toggle behavior (tap to speak, tap again to immediately mute/cancel).
 *
 * Dependencies:
 * - [android.speech.tts.TextToSpeech]: Android system vocalizer engine.
 * - [LocaleFormatter.getLocaleForLanguage]: Resolves dynamic i18n language codes to java.util.Locale.
 * - [rememberSeniorTtsManager]: Composable lifecycle-aware lifecycle wrapper.
 * =========================================================================================
 */
class SeniorTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val TAG = "SeniorTtsManager"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    /** Reactive StateFlow observing whether speech is actively playing */
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to instantiate TextToSpeech", e)
        }
    }

    /**
     * Callback from Android TTS engine upon service readiness.
     * Attaches [UtteranceProgressListener] to monitor utterance completion or errors.
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.w(TAG, "TextToSpeech init returned status: $status")
            isInitialized = false
        }
    }

    /**
     * Speaks the given text using the appropriate locale synthesizer.
     *
     * Features:
     * - Toggle logic: If currently speaking, stops audio immediately and resets state.
     * - Language localization: Adapts engine voice to [languageCode] via [LocaleFormatter].
     * - Fallback: Defaults to Locale.US if language voice data is unavailable.
     *
     * @param text Localized sentence to be read aloud.
     * @param languageCode Active language identifier (e.g. "en", "es", "fr", "de", "it", "ja").
     */
    fun speak(text: String, languageCode: String) {
        val engine = tts ?: return

        try {
            if (_isSpeaking.value) {
                engine.stop()
                _isSpeaking.value = false
                return
            }

            if (!isInitialized) {
                Log.w(TAG, "TTS not ready yet")
                return
            }

            val targetLocale = LocaleFormatter.getLocaleForLanguage(languageCode)
            val langResult = engine.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.US)
            }

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "senior_totals_utterance")
            }

            _isSpeaking.value = true
            val res = engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "senior_totals_utterance")
            if (res != TextToSpeech.SUCCESS) {
                _isSpeaking.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking speech playback", e)
            _isSpeaking.value = false
        }
    }

    /** Stops current vocal playback and resets speech state */
    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    /** Releases underlying TextToSpeech resources and listeners */
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }
}

/**
 * Creates and remembers a [SeniorTtsManager] instance scoped to the Composable lifecycle.
 * Automatically triggers [SeniorTtsManager.shutdown] when leaving composition via [DisposableEffect].
 *
 * @return [SeniorTtsManager] instance.
 */
@Composable
fun rememberSeniorTtsManager(): SeniorTtsManager {
    val context = LocalContext.current
    val ttsManager = remember(context) { SeniorTtsManager(context) }

    DisposableEffect(ttsManager) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    return ttsManager
}

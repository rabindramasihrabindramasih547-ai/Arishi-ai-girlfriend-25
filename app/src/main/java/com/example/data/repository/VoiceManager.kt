package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.VoiceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val scope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private val TAG = "VoiceManager"

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onAmplitudeUpdated: ((Float) -> Unit)? = null

    var voicePitch: Float = 1.25f // Soft, cute female pitch
    var speechRate: Float = 1.0f
    var voiceVolume: Float = 1.0f

    private var amplitudeJob: Job? = null

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            setupFemaleVoice()
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _voiceState.value = VoiceState.SPEAKING
                    startSimulatedAmplitude()
                }

                override fun onDone(utteranceId: String?) {
                    stopAmplitude()
                    _voiceState.value = VoiceState.IDLE
                }

                override fun onError(utteranceId: String?) {
                    stopAmplitude()
                    _voiceState.value = VoiceState.IDLE
                }
            })
            Log.d(TAG, "TTS Initialized successfully")
        } else {
            Log.e(TAG, "TTS Initialization failed")
        }
    }

    private fun setupFemaleVoice() {
        textToSpeech?.let { tts ->
            // Check for Indian English or Hindi locale
            val hindi = Locale("hi", "IN")
            val engInd = Locale("en", "IN")

            if (tts.isLanguageAvailable(hindi) >= TextToSpeech.LANG_AVAILABLE) {
                tts.language = hindi
            } else if (tts.isLanguageAvailable(engInd) >= TextToSpeech.LANG_AVAILABLE) {
                tts.language = engInd
            } else {
                tts.language = Locale.getDefault()
            }

            // Look for female voice variant if available
            try {
                val femaleVoice = tts.voices?.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    name.contains("female") || name.contains("in-language") || name.contains("hi-in-f")
                }
                if (femaleVoice != null) {
                    tts.voice = femaleVoice
                }
            } catch (e: Exception) {
                Log.w(TAG, "Custom voice selection not available: ${e.message}")
            }

            tts.setPitch(voicePitch)
            tts.setSpeechRate(speechRate)
        }
    }

    /**
     * Starts continuous listening on the microphone.
     */
    fun startListening(languageCode: String = "hi-IN") {
        stopSpeaking() // Natural interruption: user speaking cancels AI playback

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition unavailable on device")
            _voiceState.value = VoiceState.ERROR
            return
        }

        scope.launch(Dispatchers.Main) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = VoiceState.LISTENING
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = VoiceState.LISTENING
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize microphone input RMS into 0..1 for avatar and visualizer
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _audioAmplitude.value = normalized
                        onAmplitudeUpdated?.invoke(normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = VoiceState.THINKING
                    }

                    override fun onError(error: Int) {
                        Log.w(TAG, "Speech recognition error code: $error")
                        _voiceState.value = VoiceState.IDLE
                        _audioAmplitude.value = 0f
                        onAmplitudeUpdated?.invoke(0f)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull().orEmpty()
                        _audioAmplitude.value = 0f
                        onAmplitudeUpdated?.invoke(0f)

                        if (text.isNotBlank()) {
                            _recognizedText.value = text
                            _voiceState.value = VoiceState.THINKING
                            onSpeechRecognized?.invoke(text)
                        } else {
                            _voiceState.value = VoiceState.IDLE
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull().orEmpty()
                        if (partial.isNotBlank()) {
                            _recognizedText.value = partial
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-IN", "hi-IN", "en-US"))
                }

                _voiceState.value = VoiceState.CONNECTING
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting speech recognition", e)
                _voiceState.value = VoiceState.ERROR
            }
        }
    }

    fun stopListening() {
        scope.launch(Dispatchers.Main) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping listener", e)
            }
            if (_voiceState.value == VoiceState.LISTENING) {
                _voiceState.value = VoiceState.IDLE
            }
            _audioAmplitude.value = 0f
            onAmplitudeUpdated?.invoke(0f)
        }
    }

    /**
     * Speaks the given text using Arishi's cute, soft voice and drives
     * the 3D avatar's mouth animation in real time.
     */
    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        if (!isTtsReady || textToSpeech == null) {
            Log.w(TAG, "TTS not ready")
            return
        }

        // Clean out action asterisks (e.g. "*smiles*", "*blushes*") for voice speech
        val cleanText = text.replace(Regex("\\*.*?\\*"), "").trim()
        if (cleanText.isBlank()) return

        textToSpeech?.let { tts ->
            tts.setPitch(voicePitch)
            tts.setSpeechRate(speechRate)

            val params = Bundle()
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, voiceVolume)

            _voiceState.value = VoiceState.SPEAKING
            tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "ArishiVoiceUtterance_${System.currentTimeMillis()}")
        }
    }

    /**
     * Interrupts and stops ongoing speech immediately.
     */
    fun stopSpeaking() {
        stopAmplitude()
        textToSpeech?.stop()
        if (_voiceState.value == VoiceState.SPEAKING) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    private fun startSimulatedAmplitude() {
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch(Dispatchers.Default) {
            var phase = 0f
            while (isActive && _voiceState.value == VoiceState.SPEAKING) {
                // Natural speech amplitude waveform modulation
                phase += 0.35f
                val base = (Math.sin(phase.toDouble()) * 0.5 + 0.5).toFloat()
                val flutter = (Math.sin(phase * 2.7) * 0.25).toFloat()
                val amp = (base + flutter).coerceIn(0.15f, 0.95f)

                _audioAmplitude.value = amp
                onAmplitudeUpdated?.invoke(amp)
                delay(60L) // 16 FPS modulation matching phoneme transitions
            }
            _audioAmplitude.value = 0f
            onAmplitudeUpdated?.invoke(0f)
        }
    }

    private fun stopAmplitude() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _audioAmplitude.value = 0f
        onAmplitudeUpdated?.invoke(0f)
    }

    fun release() {
        stopSpeaking()
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}

package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.CompanionMode
import com.example.data.model.MemoryItem
import com.example.data.model.UserProfile
import com.example.data.model.VoiceState
import com.example.data.repository.ArishiAiService
import com.example.data.repository.ArishiRepository
import com.example.data.repository.VoiceManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "MainViewModel"

    private val repository = ArishiRepository(application.applicationContext)
    private val aiService = ArishiAiService()
    val voiceManager = VoiceManager(application.applicationContext, viewModelScope)

    // User Profile
    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Long-Term Memories
    val memories: StateFlow<List<MemoryItem>> = repository.observeMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat History
    val messages: StateFlow<List<ChatMessage>> = repository.observeMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _currentMode = MutableStateFlow(CompanionMode.NORMAL)
    val currentMode: StateFlow<CompanionMode> = _currentMode.asStateFlow()

    private val _avatarEmotion = MutableStateFlow("happy")
    val avatarEmotion: StateFlow<String> = _avatarEmotion.asStateFlow()

    private val _avatarTheme = MutableStateFlow("default")
    val avatarTheme: StateFlow<String> = _avatarTheme.asStateFlow()

    private val _isVoiceActive = MutableStateFlow(false)
    val isVoiceActive: StateFlow<Boolean> = _isVoiceActive.asStateFlow()

    private val _isFullScreenAvatar = MutableStateFlow(false)
    val isFullScreenAvatar: StateFlow<Boolean> = _isFullScreenAvatar.asStateFlow()

    private val _suggestedGreeting = MutableStateFlow("")
    val suggestedGreeting: StateFlow<String> = _suggestedGreeting.asStateFlow()

    val voiceState: StateFlow<VoiceState> = voiceManager.voiceState
    val audioAmplitude: StateFlow<Float> = voiceManager.audioAmplitude

    init {
        // Wire Speech Recognition callbacks
        voiceManager.onSpeechRecognized = { text ->
            processUserMessage(text, isVoice = true)
        }

        // Initialize greeting
        updateGreeting()

        // Sync mode and voice from profile
        viewModelScope.launch {
            userProfile.collect { profile ->
                if (profile != null) {
                    _currentMode.value = CompanionMode.fromId(profile.currentMode)
                    _avatarTheme.value = profile.avatarTheme
                    voiceManager.speechRate = profile.voiceSpeed.toFloat()
                    voiceManager.voiceVolume = profile.voiceVolume.toFloat()
                } else {
                    // Initialize default profile if needed
                    val user = Firebase.auth.currentUser
                    if (user != null) {
                        repository.saveUserProfile(
                            displayName = user.displayName ?: "Jaan",
                            preferredLanguage = "auto",
                            currentMode = "normal"
                        )
                    }
                }
            }
        }
    }

    private fun updateGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        _suggestedGreeting.value = when (hour) {
            in 5..11 -> "Good morning jaan! Aaj ka plan kya hai? ☀️"
            in 12..16 -> "Good afternoon! Kaisa chal raha hai tumhara din? ✨"
            in 17..21 -> "Good evening! Tumse baat karne ka intezaar tha. ❤️"
            else -> "Itni raat ko jage ho? Sab theek na, jaan? 🌙"
        }
    }

    fun toggleVoiceMode() {
        if (_isVoiceActive.value) {
            _isVoiceActive.value = false
            voiceManager.stopListening()
            voiceManager.stopSpeaking()
        } else {
            _isVoiceActive.value = true
            val lang = when (userProfile.value?.preferredLanguage) {
                "hi", "hinglish" -> "hi-IN"
                "en" -> "en-IN"
                else -> "hi-IN"
            }
            voiceManager.startListening(lang)
        }
    }

    fun toggleFullScreenAvatar() {
        _isFullScreenAvatar.value = !_isFullScreenAvatar.value
    }

    fun setMode(mode: CompanionMode) {
        _currentMode.value = mode
        _avatarEmotion.value = when (mode) {
            CompanionMode.ROMANTIC -> "romantic"
            CompanionMode.CUTE -> "playful"
            CompanionMode.EMOTIONAL_SUPPORT -> "supportive"
            CompanionMode.FUN -> "playful"
            else -> "happy"
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMode(mode.id)
        }
    }

    fun setTheme(themeId: String) {
        _avatarTheme.value = themeId
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        processUserMessage(text.trim(), isVoice = false)
    }

    private fun processUserMessage(userText: String, isVoice: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = Firebase.auth.currentUser
            val userName = userProfile.value?.displayName ?: user?.displayName ?: "Jaan"
            val mode = _currentMode.value
            val lang = userProfile.value?.preferredLanguage ?: "auto"
            val memList = memories.value

            // 1. Save user message to Firestore
            repository.saveMessage(
                sender = "user",
                text = userText,
                mode = mode.id,
                language = lang,
                isVoice = isVoice
            )

            // 2. Extract potential memory in background
            launch {
                try {
                    val candidate = aiService.extractMemoryCandidate(userText)
                    if (candidate != null) {
                        repository.addMemory(
                            category = candidate.first,
                            key = candidate.second,
                            value = candidate.third,
                            isApproved = true
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Memory extraction error: ${e.message}")
                }
            }

            // 3. Generate Arishi response
            val replyText = aiService.generateResponse(
                userInput = userText,
                history = messages.value,
                userName = userName,
                mode = mode,
                memories = memList,
                preferredLanguage = lang
            )

            // 4. Save Arishi message to Firestore
            repository.saveMessage(
                sender = "arishi",
                text = replyText,
                mode = mode.id,
                language = lang,
                isVoice = isVoice
            )

            // 5. Speak response via TTS if in voice mode or requested
            if (isVoice || _isVoiceActive.value) {
                launch(Dispatchers.Main) {
                    voiceManager.speak(replyText) {
                        // After speaking, resume listening if voice mode is still active
                        if (_isVoiceActive.value) {
                            val speechLang = if (lang == "en") "en-IN" else "hi-IN"
                            voiceManager.startListening(speechLang)
                        }
                    }
                }
            }
        }
    }

    fun interruptSpeech() {
        voiceManager.stopSpeaking()
        voiceManager.stopListening()
    }

    // Memory operations
    fun addMemory(category: String, key: String, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMemory(category, key, value, isApproved = true)
        }
    }

    fun toggleMemoryApproval(memoryId: String, isApproved: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMemoryApproval(memoryId, isApproved)
        }
    }

    fun deleteMemory(memoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMemory(memoryId)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllMemories()
        }
    }

    // Chat operations
    fun deleteChatMessage(messageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMessage(messageId)
        }
    }

    fun clearConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearConversation()
        }
    }

    // Settings
    fun saveVoiceSettings(speed: Float, pitch: Float, volume: Float, language: String) {
        voiceManager.speechRate = speed
        voiceManager.voicePitch = pitch
        voiceManager.voiceVolume = volume
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVoiceSettings(speed.toDouble(), volume.toDouble(), language)
        }
    }

    fun testVoice(speed: Float, pitch: Float) {
        voiceManager.speechRate = speed
        voiceManager.voicePitch = pitch
        voiceManager.speak("Hey jaan! Tumse baat karke bohot achha lag raha hai.")
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}

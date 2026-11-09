package com.example.data.model

enum class VoiceState(val label: String) {
    IDLE("Arishi is here"),
    CONNECTING("Connecting..."),
    LISTENING("Listening to you..."),
    THINKING("Thinking..."),
    SPEAKING("Arishi is speaking..."),
    INTERRUPTED("Listening again..."),
    RECONNECTING("Reconnecting..."),
    ERROR("Connection error")
}

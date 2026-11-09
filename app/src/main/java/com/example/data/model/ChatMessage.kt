package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sender: String = "user", // "user" or "arishi"
    val text: String = "",
    val mode: String = "normal",
    val language: String = "en",
    val isVoice: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

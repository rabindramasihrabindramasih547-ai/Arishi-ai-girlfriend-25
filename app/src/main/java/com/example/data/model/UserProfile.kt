package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "User",
    val preferredLanguage: String = "auto",
    val currentMode: String = "normal",
    val voiceName: String = "cute_soft_female",
    val voiceSpeed: Double = 1.0,
    val voiceVolume: Double = 1.0,
    val avatarTheme: String = "default",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

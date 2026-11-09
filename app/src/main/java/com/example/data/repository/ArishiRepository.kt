package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.MemoryItem
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ArishiRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth
    private val TAG = "ArishiRepository"

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Arishi AI data.")
    }

    // --- User Profile ---

    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    Log.e(TAG, "Failed to observe profile: ${e.message}", e)
                    emit(null)
                }
        )
    }

    suspend fun saveUserProfile(displayName: String, preferredLanguage: String, currentMode: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            val payload = mapOf(
                "userId" to uid,
                "displayName" to displayName,
                "preferredLanguage" to preferredLanguage,
                "currentMode" to currentMode,
                "voiceName" to "cute_soft_female",
                "voiceSpeed" to 1.0,
                "voiceVolume" to 1.0,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } else {
            val updates = mapOf(
                "displayName" to displayName,
                "preferredLanguage" to preferredLanguage,
                "currentMode" to currentMode,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
        }
    }

    suspend fun updateMode(modeId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).update(
            mapOf(
                "currentMode" to modeId,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun updateVoiceSettings(voiceSpeed: Double, voiceVolume: Double, language: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).update(
            mapOf(
                "voiceSpeed" to voiceSpeed,
                "voiceVolume" to voiceVolume,
                "preferredLanguage" to language,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // --- Long-Term Memories ---

    fun observeMemories(): Flow<List<MemoryItem>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("users").document(uid).collection("memories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(MemoryItem::class.java) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    Log.e(TAG, "Error observing memories: ${e.message}", e)
                    emit(emptyList())
                }
        )
    }

    suspend fun addMemory(category: String, key: String, value: String, isApproved: Boolean = true) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("memories").document()
        val payload = mapOf(
            "userId" to uid,
            "category" to category,
            "key" to key,
            "value" to value,
            "isApproved" to isApproved,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    suspend fun updateMemoryApproval(memoryId: String, isApproved: Boolean) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("memories").document(memoryId).update(
            mapOf(
                "isApproved" to isApproved,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun deleteMemory(memoryId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("memories").document(memoryId).delete().await()
    }

    suspend fun clearAllMemories() {
        val uid = requireUserId()
        val collection = db.collection("users").document(uid).collection("memories")
        val snapshots = collection.get().await()
        for (doc in snapshots.documents) {
            doc.reference.delete().await()
        }
    }

    // --- Chat Messages ---

    fun observeMessages(limit: Long = 100): Flow<List<ChatMessage>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("users").document(uid).collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .snapshots()
                .map { snapshot ->
                    // Return chronological order for UI display
                    snapshot.toObjects(ChatMessage::class.java).reversed()
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    Log.e(TAG, "Error observing messages: ${e.message}", e)
                    emit(emptyList())
                }
        )
    }

    suspend fun saveMessage(
        sender: String,
        text: String,
        mode: String,
        language: String = "auto",
        isVoice: Boolean = false
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("messages").document()
        val payload = mapOf(
            "userId" to uid,
            "sender" to sender,
            "text" to text,
            "mode" to mode,
            "language" to language,
            "isVoice" to isVoice,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    suspend fun deleteMessage(messageId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("messages").document(messageId).delete().await()
    }

    suspend fun clearConversation() {
        val uid = requireUserId()
        val collection = db.collection("users").document(uid).collection("messages")
        val snapshots = collection.get().await()
        for (doc in snapshots.documents) {
            doc.reference.delete().await()
        }
    }
}

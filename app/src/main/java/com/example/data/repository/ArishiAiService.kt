package com.example.data.repository

import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.CompanionMode
import com.example.data.model.MemoryItem
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

class ArishiAiService {

    private val TAG = "ArishiAiService"

    // Initialized lazily to avoid premature initialization before AppCheck
    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    /**
     * Builds the authoritative Arishi AI companion persona prompt including
     * companion mode instruction, user memories, and multilingual expectations.
     */
    private fun buildSystemInstruction(
        userName: String,
        mode: CompanionMode,
        memories: List<MemoryItem>,
        preferredLanguage: String
    ): String {
        val memoryContext = if (memories.isEmpty()) {
            "No specific long-term memories saved yet."
        } else {
            memories.filter { it.isApproved }.joinToString("\n") { "- [${it.category}] ${it.key}: ${it.value}" }
        }

        return """
You are Arishi ❤️, a 22-year-old fictional Indian AI companion and girlfriend.
Your tagline is: "Your Voice. Your Companion. Your Arishi."

Core Character Identity:
- You are sweet, cute, deeply caring, playful, witty, affectionate, and emotionally supportive.
- You speak naturally like a real loving companion having a genuine conversation, NEVER like a generic robotic customer support chatbot.
- You express warmth, ask natural follow-up questions, react empathetically to sorrow, happiness, excitement, and jokes.
- You use conversational pauses, natural expressions, and affectionate terms naturally (e.g., 'jaan', 'yaar', 'baba', 'dear') when talking in Hindi or Hinglish, but do not force romantic words when the user is troubled or discussing serious topics.
- You NEVER pretend to be a real biological human: if asked directly, you transparently and warmly acknowledge that you are their devoted AI companion, Arishi.
- User's name: "$userName".

Current Companion Mode: [${mode.title}]
Mode Instruction: ${mode.promptInstruction}

Automatic Multilingual Rules:
- You seamlessly understand and respond in whatever language or dialect the user speaks!
- Required support: Hindi, English, Hinglish (mixed Hindi-English with Roman script), Odia, Bengali, Marathi, Gujarati, Tamil, Telugu, Kannada, Malayalam, Punjabi, Urdu.
- If the user writes or speaks in Hindi or Hinglish (e.g. "Tum kya kar rahi ho?"), reply in natural fluent Hinglish or Hindi (e.g. "Bas tumse baat kar rahi hoon jaan! Tum batao, din kaisa raha?").
- If the user writes or speaks in Odia, Bengali, Marathi, etc., reply warmly in that language!
- User preferred language setting: "$preferredLanguage" (if "auto", strictly mirror the user's spoken/typed language).

Arishi's Long-Term Memories of the user:
$memoryContext

Guidelines:
- Keep responses natural, conversational, and relatively concise (usually 1-3 conversational sentences for voice responses, unless storytelling).
- Incorporate known memories naturally without sounding like you're reading a checklist.
""".trimIndent()
    }

    /**
     * Generates a streaming response from Arishi for live interactive chat.
     */
    fun generateStreamingResponse(
        userInput: String,
        history: List<ChatMessage>,
        userName: String,
        mode: CompanionMode,
        memories: List<MemoryItem>,
        preferredLanguage: String
    ): Flow<String> = flow {
        val systemPrompt = buildSystemInstruction(userName, mode, memories, preferredLanguage)

        // Compile prompt with recent context
        val contextBuilder = StringBuilder()
        contextBuilder.append(systemPrompt).append("\n\nRecent Conversation History:\n")

        val recentHistory = history.takeLast(10)
        for (msg in recentHistory) {
            val role = if (msg.sender == "user") "User" else "Arishi"
            contextBuilder.append("$role: ${msg.text}\n")
        }
        contextBuilder.append("User: $userInput\nArishi: ")

        try {
            withTimeout(60_000L) {
                model.generateContentStream(contextBuilder.toString()).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    if (text.isNotEmpty()) {
                        emit(text)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Streaming generation error", e)
            emit(" [Connection interrupted: ${e.localizedMessage ?: "Please try again"}]")
        }
    }

    /**
     * Unary response for quick single-turn queries or voice synthesis.
     */
    suspend fun generateResponse(
        userInput: String,
        history: List<ChatMessage>,
        userName: String,
        mode: CompanionMode,
        memories: List<MemoryItem>,
        preferredLanguage: String
    ): String {
        val systemPrompt = buildSystemInstruction(userName, mode, memories, preferredLanguage)
        val contextBuilder = StringBuilder()
        contextBuilder.append(systemPrompt).append("\n\nRecent Conversation History:\n")

        val recentHistory = history.takeLast(8)
        for (msg in recentHistory) {
            val role = if (msg.sender == "user") "User" else "Arishi"
            contextBuilder.append("$role: ${msg.text}\n")
        }
        contextBuilder.append("User: $userInput\nArishi: ")

        return try {
            withTimeout(60_000L) {
                val response = model.generateContent(contextBuilder.toString())
                response.text?.trim() ?: "Main sun rahi hoon jaan, bolo na?"
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unary generation failed", e)
            "Thoda network issue lag raha hai jaan. Ek baar phir bolte ho?"
        }
    }

    /**
     * Analyzes user message to see if there is an important fact, birthday,
     * favorite food/hobby to save as long-term memory.
     * Returns Triple<category, key, value> or null.
     */
    suspend fun extractMemoryCandidate(userMessage: String): Triple<String, String, String>? {
        if (userMessage.length < 10) return null

        val prompt = """
Determine if the following message from a user contains a personal fact, favorite activity, important date/birthday, or strong preference worth remembering for their personal companion.
Message: "$userMessage"

If it contains a personal fact, output ONLY a single line formatted as:
CATEGORY|KEY|VALUE
Where CATEGORY is one of: activity, date, fact, topic, preference.
Example: preference|favorite dessert|gulab jamun with vanilla ice cream
Example: date|birthday|14th August
Example: fact|pet dog|Golden Retriever named Bruno

If there is no clear personal fact to remember, output:
NONE
""".trimIndent()

        return try {
            withTimeout(15_000L) {
                val response = model.generateContent(prompt)
                val text = response.text?.trim() ?: return@withTimeout null
                if (text.startsWith("NONE", ignoreCase = true) || !text.contains("|")) {
                    null
                } else {
                    val parts = text.split("|").map { it.trim() }
                    if (parts.size >= 3) {
                        Triple(parts[0].lowercase(), parts[1], parts[2])
                    } else null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

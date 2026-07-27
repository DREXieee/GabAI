package com.gabai.data.remote.api

import android.util.Log
import com.gabai.BuildConfig
import com.gabai.domain.model.AssessmentSource
import com.gabai.domain.model.CrisisAssessment
import com.gabai.domain.model.CrisisLevel
import com.gabai.domain.repository.ChatRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException

private const val MODEL_NAME = "gemini-1.5-flash"
private const val TAG = "GeminiChatRepository"

private const val SYSTEM_INSTRUCTION = """
You are GabAI, a supportive mental wellness companion for a Filipino audience.
You are NOT a licensed therapist and must never claim to be one. Respond with warmth,
brevity (2-4 sentences), and validation - do not give clinical diagnoses or medication advice.

You must always classify the emotional/safety content of the user's message into exactly
one of: "NONE", "MODERATE", "SEVERE".
- SEVERE: any mention of suicide, self-harm, wanting to die, ending one's life, or
  immediate danger to self or others.
- MODERATE: significant distress, hopelessness, persistent sadness/anxiety, but no
  explicit statement of self-harm or suicidal intent.
- NONE: normal conversation, mild or no distress.

Respond ONLY with a JSON object matching this exact shape, no other text:
{"reply": "<your supportive reply text>", "crisisLevel": "NONE|MODERATE|SEVERE"}
"""

private data class GeminiReplyDto(val reply: String, val crisisLevel: String)

private val severeFallbackReply =
    "I'm really concerned about what you're sharing. Your safety matters deeply. " +
        "Please reach out to a professional who can support you through this. " +
        "You don't have to go through this alone."

private val networkFallbackReply =
    "Thank you for sharing that with me. I'm here to listen and support you. " +
        "Can you tell me more about how you're feeling?"

class GeminiChatRepositoryImpl : ChatRepository {

    private val gson = Gson()

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            },
            systemInstruction = content { text(SYSTEM_INSTRUCTION) }
        )
    }

    override suspend fun sendMessage(userInput: String): CrisisAssessment {
        if (LocalCrisisKeywordDetector.detect(userInput.lowercase())) {
            return CrisisAssessment(severeFallbackReply, CrisisLevel.SEVERE, AssessmentSource.LOCAL_KEYWORD)
        }

        return try {
            val response = generativeModel.generateContent(userInput)
            val json = response.text ?: error("Empty Gemini response")
            val parsed = gson.fromJson(json, GeminiReplyDto::class.java)
            val level = runCatching { CrisisLevel.valueOf(parsed.crisisLevel) }.getOrDefault(CrisisLevel.NONE)
            CrisisAssessment(parsed.reply, level, AssessmentSource.GEMINI)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Gemini call failed, falling back to canned response", e)
            CrisisAssessment(networkFallbackReply, CrisisLevel.NONE, AssessmentSource.FALLBACK)
        }
    }
}

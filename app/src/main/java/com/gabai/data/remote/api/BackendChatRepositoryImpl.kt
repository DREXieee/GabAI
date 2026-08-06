package com.gabai.data.remote.api

import android.util.Base64
import android.util.Log
import com.gabai.domain.model.AssessmentSource
import com.gabai.domain.model.CrisisAssessment
import com.gabai.domain.model.CrisisLevel
import com.gabai.domain.repository.ChatRepository
import com.gabai.data.remote.models.ChatMessageRequest
import com.gabai.data.remote.models.SynthesizeSpeechRequest
import com.gabai.data.remote.models.TranscribeVoiceRequest
import kotlinx.coroutines.CancellationException

private const val TAG = "BackendChatRepository"

private val networkFallbackReply =
    "Thank you for sharing that with me. I'm here to listen and support you. " +
        "Can you tell me more about how you're feeling?"

/**
 * Replaces GeminiChatRepositoryImpl: the Claude call and its structured
 * crisis-classification now live server-side (see gabai-backend), so the API
 * key never ships in the APK. [LocalCrisisKeywordDetector] still runs first —
 * it's the fail-safe for when this backend is unreachable.
 */
class BackendChatRepositoryImpl(private val apiService: ApiService) : ChatRepository {

    private var sessionId: String? = null

    override suspend fun sendMessage(userInput: String): CrisisAssessment {
        if (LocalCrisisKeywordDetector.detect(userInput.lowercase())) {
            val severeFallbackReply =
                "I'm really concerned about what you're sharing. Your safety matters deeply. " +
                    "Please reach out to a professional who can support you through this. " +
                    "You don't have to go through this alone."
            return CrisisAssessment(severeFallbackReply, CrisisLevel.SEVERE, AssessmentSource.LOCAL_KEYWORD)
        }

        return try {
            val response = apiService.sendChatMessage(ChatMessageRequest(sessionId, userInput))
            sessionId = response.sessionId
            val level = runCatching { CrisisLevel.valueOf(response.crisisLevel) }.getOrDefault(CrisisLevel.NONE)
            CrisisAssessment(
                replyText = response.reply,
                crisisLevel = level,
                source = AssessmentSource.REMOTE_MODEL,
                sessionId = response.sessionId,
                escalationId = response.escalationId
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Backend chat call failed, falling back to canned response", e)
            CrisisAssessment(networkFallbackReply, CrisisLevel.NONE, AssessmentSource.FALLBACK)
        }
    }

    override suspend fun transcribeAudio(audioBytes: ByteArray, mimeType: String): String {
        val audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
        return apiService.transcribeVoice(TranscribeVoiceRequest(audioBase64, mimeType)).text
    }

    override suspend fun synthesizeSpeech(text: String): ByteArray {
        return apiService.synthesizeSpeech(SynthesizeSpeechRequest(text)).bytes()
    }
}

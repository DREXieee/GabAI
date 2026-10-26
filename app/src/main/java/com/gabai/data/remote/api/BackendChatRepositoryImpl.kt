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
            return CrisisAssessment(
                ReplyLanguage.inLanguageOf(userInput, ReplyLanguage.severeFallbackReply),
                CrisisLevel.SEVERE,
                AssessmentSource.LOCAL_KEYWORD
            )
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
            CrisisAssessment(
                ReplyLanguage.inLanguageOf(userInput, ReplyLanguage.networkFallbackReply),
                CrisisLevel.NONE,
                AssessmentSource.FALLBACK
            )
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

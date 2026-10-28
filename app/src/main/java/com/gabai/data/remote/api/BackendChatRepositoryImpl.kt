package com.gabai.data.remote.api

import android.util.Base64
import android.util.Log
import com.gabai.domain.model.AssessmentSource
import com.gabai.domain.model.CrisisAssessment
import com.gabai.domain.model.CrisisLevel
import com.gabai.domain.model.Emotion
import com.gabai.domain.repository.ChatRepository
import com.gabai.data.remote.models.ChatMessageRequest
import com.gabai.data.remote.models.SynthesizeSpeechRequest
import com.gabai.data.remote.models.TranscribeVoiceRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "BackendChatRepository"

/**
 * Replaces GeminiChatRepositoryImpl: the Claude call and its structured
 * crisis-classification now live server-side (see gabai-backend), so the API
 * key never ships in the APK. [LocalCrisisKeywordDetector] is the fail-safe for
 * when this backend is unreachable -- it never stops a flagged message from
 * being sent, since the backend is what saves it and auto-escalates to staff.
 */
class BackendChatRepositoryImpl(private val apiService: ApiService) : ChatRepository {

    private var sessionId: String? = null

    // One backend call at a time, so a message typed while an earlier one is
    // still in flight (Render's free tier can take 30-60s to wake) reuses that
    // call's session instead of opening a second one.
    private val sendLock = Mutex()

    override fun localCrisisAssessment(userInput: String): CrisisAssessment? {
        if (!LocalCrisisKeywordDetector.detect(userInput)) return null
        return CrisisAssessment(
            ReplyLanguage.inLanguageOf(userInput, ReplyLanguage.severeFallbackReply),
            CrisisLevel.SEVERE,
            AssessmentSource.LOCAL_KEYWORD
        )
    }

    override suspend fun sendMessage(userInput: String): CrisisAssessment = sendLock.withLock {
        try {
            val response = apiService.sendChatMessage(ChatMessageRequest(sessionId, userInput))
            sessionId = response.sessionId
            val level = runCatching { CrisisLevel.valueOf(response.crisisLevel) }.getOrDefault(CrisisLevel.NONE)
            CrisisAssessment(
                replyText = response.reply,
                crisisLevel = level,
                source = AssessmentSource.REMOTE_MODEL,
                sessionId = response.sessionId,
                escalationId = response.escalationId,
                emotion = Emotion.fromWire(response.emotion)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Backend chat call failed, falling back to canned response", e)
            // Offline, a flagged message still gets its SEVERE reply and hotlines.
            localCrisisAssessment(userInput) ?: CrisisAssessment(
                ReplyLanguage.inLanguageOf(userInput, ReplyLanguage.networkFallbackReply),
                CrisisLevel.NONE,
                AssessmentSource.FALLBACK
            )
        }
    }

    override suspend fun recentEmotions(): List<Emotion> =
        apiService.getEmotionSummary().ranked.mapNotNull(Emotion::fromWire)

    override suspend fun transcribeAudio(audioBytes: ByteArray, mimeType: String): String {
        val audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
        return apiService.transcribeVoice(TranscribeVoiceRequest(audioBase64, mimeType)).text
    }

    override suspend fun synthesizeSpeech(text: String): ByteArray {
        return apiService.synthesizeSpeech(SynthesizeSpeechRequest(text)).bytes()
    }
}

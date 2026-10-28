package com.gabai.domain.repository

import com.gabai.domain.model.CrisisAssessment

interface ChatRepository {
    // Network-free SEVERE keyword check, so the caller can show the crisis reply
    // and hotlines before sendMessage's backend call returns. Null when nothing matched.
    fun localCrisisAssessment(userInput: String): CrisisAssessment?
    suspend fun sendMessage(userInput: String): CrisisAssessment
    suspend fun transcribeAudio(audioBytes: ByteArray, mimeType: String): String
    suspend fun synthesizeSpeech(text: String): ByteArray
}

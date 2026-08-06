package com.gabai.domain.repository

import com.gabai.domain.model.CrisisAssessment

interface ChatRepository {
    suspend fun sendMessage(userInput: String): CrisisAssessment
    suspend fun transcribeAudio(audioBytes: ByteArray, mimeType: String): String
    suspend fun synthesizeSpeech(text: String): ByteArray
}

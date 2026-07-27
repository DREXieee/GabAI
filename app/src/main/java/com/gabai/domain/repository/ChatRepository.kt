package com.gabai.domain.repository

import com.gabai.domain.model.CrisisAssessment

interface ChatRepository {
    suspend fun sendMessage(userInput: String): CrisisAssessment
}

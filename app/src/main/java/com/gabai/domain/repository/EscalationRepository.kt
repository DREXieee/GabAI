package com.gabai.domain.repository

import com.gabai.domain.model.CrisisLevel
import kotlinx.coroutines.flow.Flow

data class EscalationEvent(
    val id: String,
    val crisisLevel: CrisisLevel,
    val triggeringMessage: String,
    val timestampEpochMillis: Long,
    val requestedHumanSupport: Boolean
)

data class EscalationConfirmation(
    val estimatedResponseMinutes: Int,
    val requestId: String
)

interface EscalationRepository {
    suspend fun logEscalationEvent(event: EscalationEvent)
    suspend fun requestHumanSupport(event: EscalationEvent): EscalationConfirmation
    fun observeEscalationHistory(): Flow<List<EscalationEvent>>
}

package com.gabai.data.remote.api

import com.gabai.data.remote.models.CreateEscalationRequest
import com.gabai.domain.model.CrisisLevel
import com.gabai.domain.repository.EscalationRepository
import com.gabai.domain.repository.EscalationStatusUpdate
import com.gabai.domain.repository.RemoteEscalationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val POLL_INTERVAL_MS = 3000L
private val TERMINAL_STATUSES = setOf(RemoteEscalationStatus.RESOLVED, RemoteEscalationStatus.ABANDONED)

class RemoteEscalationRepositoryImpl(private val apiService: ApiService) : EscalationRepository {

    override suspend fun requestHumanSupport(sessionId: String, crisisLevel: CrisisLevel): String {
        val response = apiService.createEscalation(CreateEscalationRequest(sessionId, crisisLevel.name))
        return response.id
    }

    override fun observeEscalationStatus(escalationId: String): Flow<EscalationStatusUpdate> = flow {
        while (true) {
            val response = apiService.getEscalationStatus(escalationId)
            val status = runCatching { RemoteEscalationStatus.valueOf(response.status) }
                .getOrDefault(RemoteEscalationStatus.PENDING)
            val crisisLevel = runCatching { CrisisLevel.valueOf(response.crisisLevel) }
                .getOrDefault(CrisisLevel.NONE)

            emit(
                EscalationStatusUpdate(
                    id = response.id,
                    status = status,
                    crisisLevel = crisisLevel,
                    claimedByStaffName = response.claimedByStaffName,
                    dailyRoomUrl = response.dailyRoomUrl,
                    patientMeetingToken = response.patientMeetingToken
                )
            )

            if (status in TERMINAL_STATUSES) break
            delay(POLL_INTERVAL_MS)
        }
    }
}

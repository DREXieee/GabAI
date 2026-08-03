package com.gabai.domain.repository

import com.gabai.domain.model.Clinic
import com.gabai.domain.model.CrisisLevel
import kotlinx.coroutines.flow.Flow

enum class RemoteEscalationStatus { PENDING, CLAIMED, IN_CALL, TRANSFERRED, RESOLVED, ABANDONED }

data class EscalationStatusUpdate(
    val id: String,
    val status: RemoteEscalationStatus,
    val crisisLevel: CrisisLevel,
    val claimedByStaffName: String?,
    val dailyRoomUrl: String? = null,
    val patientMeetingToken: String? = null,
    val transferredDoctorClinic: Clinic? = null
)

interface EscalationRepository {
    /** Patient explicitly asking for a human (no server-side auto-detection involved). */
    suspend fun requestHumanSupport(sessionId: String, crisisLevel: CrisisLevel): String

    /** Polls the backend until the escalation resolves; emits every status change along the way. */
    fun observeEscalationStatus(escalationId: String): Flow<EscalationStatusUpdate>
}

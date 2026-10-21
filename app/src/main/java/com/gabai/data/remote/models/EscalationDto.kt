package com.gabai.data.remote.models

data class CreateEscalationRequest(
    // Null when asking from the dashboard before any chat exists; the server
    // then opens a session itself.
    val sessionId: String?,
    val crisisLevel: String
)

data class EscalationStatusDto(
    val id: String,
    val status: String,
    val crisisLevel: String,
    val claimedByStaffName: String?,
    val createdAt: String,
    val claimedAt: String?,
    val resolvedAt: String?,
    val dailyRoomUrl: String? = null,
    val patientMeetingToken: String? = null,
    val transferredDoctorClinic: ClinicDto? = null
)

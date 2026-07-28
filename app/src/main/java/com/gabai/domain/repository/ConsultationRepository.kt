package com.gabai.domain.repository

import kotlinx.coroutines.flow.Flow

enum class RemoteConsultationStatus { CONFIRMED, CANCELLED, COMPLETED }

data class AvailableSlot(
    val id: String,
    val doctorId: String,
    val doctorName: String,
    val startTime: String,
    val endTime: String
)

data class ConsultationSummary(
    val id: String,
    val status: RemoteConsultationStatus,
    val doctorName: String,
    val slotStartTime: String,
    val slotEndTime: String
)

data class ConsultationStatusUpdate(
    val id: String,
    val status: RemoteConsultationStatus,
    val doctorName: String,
    val slotStartTime: String,
    val slotEndTime: String,
    val reasonNote: String?,
    val dailyRoomUrl: String? = null,
    val patientMeetingToken: String? = null
)

interface ConsultationRepository {
    suspend fun listOpenSlots(): List<AvailableSlot>

    /** Books an open slot; returns the new consultation's id. */
    suspend fun bookSlot(slotId: String, reasonNote: String?): String

    suspend fun listMyConsultations(): List<ConsultationSummary>

    /** Polls the backend until the consultation reaches a terminal state; emits every change along the way. */
    fun observeConsultationStatus(consultationId: String): Flow<ConsultationStatusUpdate>

    suspend fun cancelConsultation(consultationId: String)
}

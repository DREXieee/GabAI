package com.gabai.data.remote.api

import com.gabai.data.remote.models.BookSlotRequest
import com.gabai.data.remote.models.CancelConsultationRequest
import com.gabai.domain.repository.AvailableSlot
import com.gabai.domain.repository.ConsultationRepository
import com.gabai.domain.repository.ConsultationStatusUpdate
import com.gabai.domain.repository.ConsultationSummary
import com.gabai.domain.repository.RemoteConsultationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val POLL_INTERVAL_MS = 5000L
private val TERMINAL_STATUSES = setOf(RemoteConsultationStatus.CANCELLED, RemoteConsultationStatus.COMPLETED)

class RemoteConsultationRepositoryImpl(private val apiService: ApiService) : ConsultationRepository {

    override suspend fun listOpenSlots(): List<AvailableSlot> {
        return apiService.listOpenSlots().slots.map { slot ->
            AvailableSlot(
                id = slot.id,
                doctorId = slot.doctorId,
                doctorName = slot.doctor?.displayName ?: "Doctor",
                startTime = slot.startTime,
                endTime = slot.endTime
            )
        }
    }

    override suspend fun bookSlot(slotId: String, reasonNote: String?): String {
        return apiService.bookSlot(BookSlotRequest(slotId, reasonNote)).id
    }

    override suspend fun listMyConsultations(): List<ConsultationSummary> {
        return apiService.listMyConsultations().consultations.map { c ->
            ConsultationSummary(
                id = c.id,
                status = runCatching { RemoteConsultationStatus.valueOf(c.status) }
                    .getOrDefault(RemoteConsultationStatus.CONFIRMED),
                doctorName = c.doctor.displayName,
                slotStartTime = c.slot.startTime,
                slotEndTime = c.slot.endTime
            )
        }
    }

    override fun observeConsultationStatus(consultationId: String): Flow<ConsultationStatusUpdate> = flow {
        while (true) {
            val response = apiService.getConsultationStatus(consultationId)
            val status = runCatching { RemoteConsultationStatus.valueOf(response.status) }
                .getOrDefault(RemoteConsultationStatus.CONFIRMED)

            emit(
                ConsultationStatusUpdate(
                    id = response.id,
                    status = status,
                    doctorName = response.doctorName,
                    slotStartTime = response.slotStartTime,
                    slotEndTime = response.slotEndTime,
                    reasonNote = response.reasonNote,
                    dailyRoomUrl = response.dailyRoomUrl,
                    patientMeetingToken = response.patientMeetingToken
                )
            )

            if (status in TERMINAL_STATUSES) break
            delay(POLL_INTERVAL_MS)
        }
    }

    override suspend fun cancelConsultation(consultationId: String) {
        apiService.cancelConsultation(consultationId, CancelConsultationRequest())
    }
}

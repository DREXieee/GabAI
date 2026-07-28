package com.gabai.data.remote.models

data class ConsultationDoctorDto(
    val id: String,
    val displayName: String
)

data class ConsultationSlotDto(
    val id: String,
    val doctorId: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val createdAt: String,
    val doctor: ConsultationDoctorDto? = null
)

data class OpenSlotsResponse(val slots: List<ConsultationSlotDto>)

data class BookSlotRequest(
    val slotId: String,
    val reasonNote: String? = null
)

data class ConsultationCallSessionDto(
    val id: String,
    val dailyRoomUrl: String,
    val endedAt: String? = null
)

data class ConsultationDto(
    val id: String,
    val slotId: String,
    val patientId: String,
    val doctorId: String,
    val status: String,
    val reasonNote: String?,
    val cancelledAt: String?,
    val createdAt: String,
    val slot: ConsultationSlotDto,
    val doctor: ConsultationDoctorDto,
    val callSession: ConsultationCallSessionDto? = null
)

data class ConsultationsResponse(val consultations: List<ConsultationDto>)

data class ConsultationStatusDto(
    val id: String,
    val status: String,
    val doctorName: String,
    val slotStartTime: String,
    val slotEndTime: String,
    val reasonNote: String?,
    val dailyRoomUrl: String?,
    val patientMeetingToken: String?,
    val callSessionId: String?
)

data class CancelConsultationRequest(val reason: String? = null)

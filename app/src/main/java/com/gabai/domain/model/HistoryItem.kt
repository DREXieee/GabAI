package com.gabai.domain.model

sealed class HistoryItem {
    abstract val date: String

    data class ChatSessionItem(
        override val date: String,
        val chatSessionId: String,
        val messageCount: Int,
        val lastMessagePreview: String?,
        val hadEscalation: Boolean
    ) : HistoryItem()

    data class EscalationItem(
        override val date: String,
        val escalationId: String,
        val crisisLevel: CrisisLevel,
        val status: String,
        val claimedByStaffName: String?
    ) : HistoryItem()

    data class ConsultationItem(
        override val date: String,
        val consultationId: String,
        val status: String,
        val doctorName: String,
        val slotStartTime: String,
        val slotEndTime: String
    ) : HistoryItem()
}

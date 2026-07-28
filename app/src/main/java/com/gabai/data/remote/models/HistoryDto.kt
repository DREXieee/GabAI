package com.gabai.data.remote.models

// Flat DTO with a `type` discriminator + nullable per-type fields -- no
// polymorphic Gson adapter is registered in RetrofitClient, so flat +
// repository-side mapping (see HistoryItem) is simpler than adding one.
data class HistoryItemDto(
    val type: String,
    val date: String,
    // CHAT_SESSION
    val chatSessionId: String? = null,
    val messageCount: Int? = null,
    val lastMessagePreview: String? = null,
    val hadEscalation: Boolean? = null,
    // ESCALATION
    val escalationId: String? = null,
    val crisisLevel: String? = null,
    val escalationStatus: String? = null,
    val claimedByStaffName: String? = null,
    // CONSULTATION
    val consultationId: String? = null,
    val consultationStatus: String? = null,
    val doctorName: String? = null,
    val slotStartTime: String? = null,
    val slotEndTime: String? = null
)

data class HistoryResponseDto(
    val items: List<HistoryItemDto>,
    val nextCursor: String?
)

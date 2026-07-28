package com.gabai.data.remote.models

data class ChatMessageDto(
    val id: String,
    val sessionId: String,
    val sender: String,
    val text: String,
    val crisisLevel: String,
    val source: String?,
    val createdAt: String
)

data class ChatSessionDetailDto(
    val id: String,
    val patientId: String,
    val startedAt: String,
    val endedAt: String?,
    val messages: List<ChatMessageDto>
)

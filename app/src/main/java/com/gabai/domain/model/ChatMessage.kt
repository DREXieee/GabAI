package com.gabai.domain.model

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val crisisLevel: CrisisLevel = CrisisLevel.NONE
)

package com.gabai.domain.model

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val crisisLevel: CrisisLevel = CrisisLevel.NONE,
    // GabAI replies only: the emotion read in the message it answered, which
    // picks the "Suggested for you" exercise shown under it.
    val emotion: Emotion? = null
)

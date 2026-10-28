package com.gabai.domain.model

data class CrisisAssessment(
    val replyText: String,
    val crisisLevel: CrisisLevel,
    val source: AssessmentSource,
    // Both null when this assessment never reached the backend (LOCAL_KEYWORD
    // fail-safe, or a FALLBACK after a failed call) -- there is no server-side
    // session/escalation to reference in that case.
    val sessionId: String? = null,
    val escalationId: String? = null,
    // What the backend's AI read in the message; null when it didn't answer.
    val emotion: Emotion? = null
)

enum class AssessmentSource { LOCAL_KEYWORD, REMOTE_MODEL, FALLBACK }

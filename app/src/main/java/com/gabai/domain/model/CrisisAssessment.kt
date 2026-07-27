package com.gabai.domain.model

data class CrisisAssessment(
    val replyText: String,
    val crisisLevel: CrisisLevel,
    val source: AssessmentSource
)

enum class AssessmentSource { LOCAL_KEYWORD, GEMINI, FALLBACK }

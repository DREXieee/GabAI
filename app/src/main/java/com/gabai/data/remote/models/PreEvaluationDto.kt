package com.gabai.data.remote.models

// gabai-backend's /api/pre-evaluation: one 0-3 answer per question, in order.
data class SubmitPreEvaluationRequest(
    val phq9: List<Int>,
    val gad7: List<Int>
)

data class PreEvaluationDto(
    val id: String,
    val phq9Score: Int,
    // MINIMAL, MILD, MODERATE, MODERATELY_SEVERE or SEVERE
    val phq9Severity: String,
    val gad7Score: Int,
    // MINIMAL, MILD, MODERATE or SEVERE
    val gad7Severity: String,
    val selfHarm: Boolean,
    val escalationId: String?,
    val createdAt: String
)

data class PreEvaluationResponse(
    // Null until the patient has answered it.
    val preEvaluation: PreEvaluationDto?
)

data class PreEvaluationEscalationDto(
    val id: String,
    val chatSessionId: String
)

data class SubmitPreEvaluationResponse(
    val preEvaluation: PreEvaluationDto,
    // The urgent request filed when question 9 was answered above "Not at all".
    val escalation: PreEvaluationEscalationDto?
)

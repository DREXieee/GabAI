package com.gabai.data.remote.api

import com.gabai.data.remote.models.PreEvaluationDto
import com.gabai.data.remote.models.SubmitPreEvaluationRequest
import com.gabai.domain.model.PreEvaluationResult
import com.gabai.domain.repository.PreEvaluationRepository

class RemotePreEvaluationRepositoryImpl(private val apiService: ApiService) : PreEvaluationRepository {

    override suspend fun getLatest(): PreEvaluationResult? =
        apiService.getPreEvaluation().preEvaluation?.toDomain(escalationSessionId = null)

    override suspend fun submit(phq9: List<Int>, gad7: List<Int>): PreEvaluationResult {
        val response = apiService.submitPreEvaluation(SubmitPreEvaluationRequest(phq9 = phq9, gad7 = gad7))
        return response.preEvaluation.toDomain(escalationSessionId = response.escalation?.chatSessionId)
    }
}

fun PreEvaluationDto.toDomain(escalationSessionId: String?): PreEvaluationResult = PreEvaluationResult(
    phq9Score = phq9Score,
    phq9Severity = phq9Severity,
    gad7Score = gad7Score,
    gad7Severity = gad7Severity,
    selfHarm = selfHarm,
    escalationId = escalationId,
    escalationSessionId = escalationSessionId
)

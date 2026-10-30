package com.gabai.domain.repository

import com.gabai.domain.model.PreEvaluationResult

interface PreEvaluationRepository {
    // Null until the patient has answered the pre-evaluation.
    suspend fun getLatest(): PreEvaluationResult?

    suspend fun submit(phq9: List<Int>, gad7: List<Int>): PreEvaluationResult
}

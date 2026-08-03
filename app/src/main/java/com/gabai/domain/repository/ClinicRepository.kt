package com.gabai.domain.repository

import com.gabai.domain.model.Clinic

interface ClinicRepository {
    suspend fun listClinics(): List<Clinic>
}

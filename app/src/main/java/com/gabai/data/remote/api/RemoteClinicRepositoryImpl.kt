package com.gabai.data.remote.api

import com.gabai.data.remote.models.ClinicDto
import com.gabai.domain.model.Clinic
import com.gabai.domain.repository.ClinicRepository

class RemoteClinicRepositoryImpl(private val apiService: ApiService) : ClinicRepository {

    override suspend fun listClinics(): List<Clinic> {
        return apiService.getClinics().clinics.map { it.toDomain() }
    }
}

fun ClinicDto.toDomain(): Clinic = Clinic(
    id = id,
    name = name,
    address = address,
    city = city,
    province = province,
    latitude = latitude,
    longitude = longitude,
    phone = phone
)

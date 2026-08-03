package com.gabai.data.remote.models

data class ClinicDto(
    val id: String,
    val name: String,
    val address: String,
    val city: String?,
    val province: String?,
    val latitude: Double,
    val longitude: Double,
    val phone: String?
)

data class ClinicsResponse(
    val clinics: List<ClinicDto>
)

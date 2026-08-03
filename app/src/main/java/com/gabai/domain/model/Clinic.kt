package com.gabai.domain.model

data class Clinic(
    val id: String,
    val name: String,
    val address: String,
    val city: String?,
    val province: String?,
    val latitude: Double,
    val longitude: Double,
    val phone: String?
)

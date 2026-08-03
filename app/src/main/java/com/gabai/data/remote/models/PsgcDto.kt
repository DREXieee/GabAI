package com.gabai.data.remote.models

// Shapes returned by the public PSGC API (psgc.gitlab.io/api) -- no API key,
// static/read-only Philippine Standard Geographic Code reference data.
data class PsgcProvinceDto(
    val code: String,
    val name: String
)

data class PsgcCityDto(
    val code: String,
    val name: String
)

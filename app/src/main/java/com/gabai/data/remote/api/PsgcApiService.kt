package com.gabai.data.remote.api

import com.gabai.data.remote.models.PsgcCityDto
import com.gabai.data.remote.models.PsgcProvinceDto
import retrofit2.http.GET
import retrofit2.http.Path

// NCR has no province level in the PSGC hierarchy -- its cities sit directly
// under the region, so "Metro Manila" is synthesized as a pseudo-province in
// the UI (see RegistrationScreen.kt) using this region code + the
// regions/{code}/cities-municipalities/ endpoint instead of the provinces one.
const val PSGC_NCR_REGION_CODE = "130000000"

interface PsgcApiService {
    @GET("provinces/")
    suspend fun getProvinces(): List<PsgcProvinceDto>

    @GET("provinces/{code}/cities-municipalities/")
    suspend fun getCitiesForProvince(@Path("code") provinceCode: String): List<PsgcCityDto>

    @GET("regions/{code}/cities-municipalities/")
    suspend fun getCitiesForRegion(@Path("code") regionCode: String): List<PsgcCityDto>
}

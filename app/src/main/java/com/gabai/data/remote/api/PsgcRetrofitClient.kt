package com.gabai.data.remote.api

import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Separate client from RetrofitClient: different host (a public, unauthenticated
// government reference-data API, not our own backend), so it needs neither the
// bearer-token interceptor nor the refresh authenticator.
object PsgcRetrofitClient {
    private const val BASE_URL = "https://psgc.gitlab.io/api/"

    val service: PsgcApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(PsgcApiService::class.java)
    }
}

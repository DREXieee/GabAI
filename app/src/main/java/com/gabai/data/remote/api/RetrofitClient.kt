package com.gabai.data.remote.api

import android.content.Context
import com.gabai.BuildConfig
import com.gabai.data.local.repository.AuthSessionStore
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile
    private var apiService: ApiService? = null

    fun getApiService(context: Context): ApiService =
        apiService ?: synchronized(this) {
            apiService ?: build(context.applicationContext).also { apiService = it }
        }

    private fun build(appContext: Context): ApiService {
        val authStore = AuthSessionStore(appContext)

        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { authStore.accessTokenOnce() }
            val original = chain.request()
            val request = if (token != null) {
                original.newBuilder().header("Authorization", "Bearer $token").build()
            } else {
                original
            }
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            // OkHttp's 10s default is too tight for /api/chat/message: the backend
            // itself calls out to Claude/Gemini for classification before replying,
            // and that round trip can run past 10s. A silent timeout here falls back
            // to a canned NONE-level reply (see BackendChatRepositoryImpl), which
            // masks real AI crisis classification behind an apparently-normal chat.
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(ApiService::class.java)
    }
}

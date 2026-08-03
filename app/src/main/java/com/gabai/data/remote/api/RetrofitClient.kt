package com.gabai.data.remote.api

import android.content.Context
import com.gabai.BuildConfig
import com.gabai.data.local.repository.AuthSessionStore
import com.gabai.data.remote.models.RefreshRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Route
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

        // A plain client/service with no auth interceptor or authenticator of its
        // own -- used only to call /api/auth/refresh from inside the authenticator
        // below without risking recursion back into itself.
        val refreshApiService = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(ApiService::class.java)

        // Without this, an expired Supabase access token meant every authenticated
        // call 401'd forever with no recovery -- the stored refresh token was never
        // actually used to get a new one. This transparently retries once with a
        // freshly refreshed token; if that also fails, the session really is dead
        // and gets cleared so the app correctly routes back to login next launch.
        val authenticator = Authenticator { _: Route?, response ->
            if (response.priorResponse != null) return@Authenticator null // already retried once

            runBlocking {
                val refreshToken = authStore.session.first()?.refreshToken
                if (refreshToken == null) {
                    null
                } else {
                    val newSession = try {
                        refreshApiService.refresh(RefreshRequest(refreshToken))
                    } catch (e: Exception) {
                        null
                    }
                    if (newSession == null) {
                        authStore.clearSession()
                        null
                    } else {
                        authStore.saveSession(newSession)
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${newSession.accessToken}")
                            .build()
                    }
                }
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(authenticator)
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

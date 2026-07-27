package com.gabai.data.remote.api

import com.gabai.data.remote.models.AuthResponse
import com.gabai.data.remote.models.ChatMessageRequest
import com.gabai.data.remote.models.ChatMessageResponse
import com.gabai.data.remote.models.CreateEscalationRequest
import com.gabai.data.remote.models.EscalationStatusDto
import com.gabai.data.remote.models.LoginRequest
import com.gabai.data.remote.models.RefreshRequest
import com.gabai.data.remote.models.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse

    @POST("api/chat/message")
    suspend fun sendChatMessage(@Body request: ChatMessageRequest): ChatMessageResponse

    @POST("api/escalations")
    suspend fun createEscalation(@Body request: CreateEscalationRequest): EscalationStatusDto

    @GET("api/escalations/{id}/status")
    suspend fun getEscalationStatus(@Path("id") id: String): EscalationStatusDto
}

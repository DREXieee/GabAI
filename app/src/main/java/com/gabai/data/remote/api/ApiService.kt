package com.gabai.data.remote.api

import com.gabai.data.remote.models.AuthResponse
import com.gabai.data.remote.models.BookSlotRequest
import com.gabai.data.remote.models.CancelConsultationRequest
import com.gabai.data.remote.models.ChatMessageRequest
import com.gabai.data.remote.models.ChatMessageResponse
import com.gabai.data.remote.models.ChatSessionDetailDto
import com.gabai.data.remote.models.ConsultationDto
import com.gabai.data.remote.models.ConsultationStatusDto
import com.gabai.data.remote.models.ConsultationsResponse
import com.gabai.data.remote.models.CreateEscalationRequest
import com.gabai.data.remote.models.EscalationStatusDto
import com.gabai.data.remote.models.HistoryResponseDto
import com.gabai.data.remote.models.LoginRequest
import com.gabai.data.remote.models.OpenSlotsResponse
import com.gabai.data.remote.models.RefreshRequest
import com.gabai.data.remote.models.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse

    @POST("api/chat/message")
    suspend fun sendChatMessage(@Body request: ChatMessageRequest): ChatMessageResponse

    @GET("api/chat/sessions/{id}")
    suspend fun getChatSessionDetail(@Path("id") id: String): ChatSessionDetailDto

    @POST("api/escalations")
    suspend fun createEscalation(@Body request: CreateEscalationRequest): EscalationStatusDto

    @GET("api/escalations/{id}/status")
    suspend fun getEscalationStatus(@Path("id") id: String): EscalationStatusDto

    @GET("api/consultations/slots")
    suspend fun listOpenSlots(@Query("doctorId") doctorId: String? = null): OpenSlotsResponse

    @POST("api/consultations")
    suspend fun bookSlot(@Body request: BookSlotRequest): ConsultationDto

    @GET("api/consultations/mine")
    suspend fun listMyConsultations(): ConsultationsResponse

    @GET("api/consultations/{id}/status")
    suspend fun getConsultationStatus(@Path("id") id: String): ConsultationStatusDto

    @PATCH("api/consultations/{id}/cancel")
    suspend fun cancelConsultation(
        @Path("id") id: String,
        @Body request: CancelConsultationRequest = CancelConsultationRequest()
    ): ConsultationDto

    @GET("api/history")
    suspend fun getHistory(@Query("cursor") cursor: String? = null, @Query("limit") limit: Int? = null): HistoryResponseDto
}

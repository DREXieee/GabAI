package com.gabai.data.remote.models

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val province: String?,
    val city: String?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class GoogleSignInRequest(
    val idToken: String
)

data class UpdateLocationRequest(
    val province: String,
    val city: String
)

data class ForgotPasswordResponse(
    val message: String
)

data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val province: String? = null,
    val city: String? = null
)

data class UpdateLocationResponse(
    val user: UserDto
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long?,
    val user: UserDto
)

data class ChatMessageRequest(
    val sessionId: String?,
    val message: String
)

data class ChatMessageResponse(
    val sessionId: String,
    val reply: String,
    val crisisLevel: String,
    val escalationId: String?
)

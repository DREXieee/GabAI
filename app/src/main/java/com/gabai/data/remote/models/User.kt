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

data class UserDto(
    val id: String,
    val email: String,
    val displayName: String
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

package com.gabai.domain.model

enum class CrisisLevel {
    NONE,      // Safe, normal conversation
    MODERATE,  // User is distressed but not in immediate danger
    SEVERE     // User mentions self-harm, suicide, or immediate danger
}

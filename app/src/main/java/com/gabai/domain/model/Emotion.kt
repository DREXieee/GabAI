package com.gabai.domain.model

// The main emotion gabai-backend's AI read in one of the patient's messages:
// one of the four the study focuses on, or NONE. A support signal for
// suggesting a Wellness exercise -- not a diagnosis, and never part of the
// crisis classification.
enum class Emotion {
    ANXIETY, DEPRESSION, FEAR, SADNESS, NONE;

    val isSuggestible: Boolean get() = this != NONE

    companion object {
        fun fromWire(value: String?): Emotion? = value?.let { runCatching { valueOf(it) }.getOrNull() }
    }
}

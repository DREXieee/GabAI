package com.gabai.data.remote.api

internal object LocalCrisisKeywordDetector {
    private val severeKeywords = listOf(
        "suicide", "want to die", "kill myself", "end my life",
        "want to end it", "no reason to live", "better off dead"
    )

    fun detect(userInputLowercase: String): Boolean =
        severeKeywords.any { userInputLowercase.contains(it) }
}

package com.gabai.domain.model

// The pre-evaluation's two standard screening questionnaires, PHQ-9 (low mood)
// and GAD-7 (anxiety), free to reproduce. Mirrors gabai-backend's
// pre-evaluation/screening.ts and gabai-web's lib/preEvaluation.ts -- change
// all three together; the backend checks there are exactly 9 and 7 answers.
object PreEvaluationQuestions {
    const val PROMPT = "Over the last 2 weeks, how often have you been bothered by:"

    val ANSWER_LABELS = listOf("Not at all", "Several days", "More than half the days", "Nearly every day")

    val PHQ9 = listOf(
        "Little interest or pleasure in doing things",
        "Feeling down, depressed, or hopeless",
        "Trouble falling or staying asleep, or sleeping too much",
        "Feeling tired or having little energy",
        "Poor appetite or overeating",
        "Feeling bad about yourself, or that you are a failure or have let yourself or your family down",
        "Trouble concentrating on things, such as reading or watching television",
        "Moving or speaking so slowly that other people could have noticed, or the opposite: being so " +
            "fidgety or restless that you have been moving around a lot more than usual",
        "Thoughts that you would be better off dead, or of hurting yourself in some way"
    )

    val GAD7 = listOf(
        "Feeling nervous, anxious, or on edge",
        "Not being able to stop or control worrying",
        "Worrying too much about different things",
        "Trouble relaxing",
        "Being so restless that it is hard to sit still",
        "Becoming easily annoyed or irritable",
        "Feeling afraid, as if something awful might happen"
    )

    val ALL = PHQ9 + GAD7

    const val PHQ9_MAX_SCORE = 27
    const val GAD7_MAX_SCORE = 21
}

// A screening result, not a diagnosis. Severities are the backend's band names
// (MINIMAL ... SEVERE).
data class PreEvaluationResult(
    val phq9Score: Int,
    val phq9Severity: String,
    val gad7Score: Int,
    val gad7Severity: String,
    val selfHarm: Boolean,
    // Set when the self-harm answer filed an urgent request for a staff member.
    val escalationId: String?,
    val escalationSessionId: String?
) {
    // Moderate or worse on either questionnaire (a score of 10+).
    val suggestsDoctor: Boolean get() = phq9Score >= 10 || gad7Score >= 10
}

fun severityLabel(severity: String): String = when (severity) {
    "MINIMAL" -> "Minimal"
    "MILD" -> "Mild"
    "MODERATE" -> "Moderate"
    "MODERATELY_SEVERE" -> "Moderately severe"
    "SEVERE" -> "Severe"
    else -> severity.lowercase().replaceFirstChar { it.uppercase() }
}

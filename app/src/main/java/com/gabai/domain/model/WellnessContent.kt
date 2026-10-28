package com.gabai.domain.model

enum class WellnessCategory(val displayName: String) {
    BREATHING("Breathing Exercises"),
    COPING_TIPS("Coping Tips"),
    ARTICLES("Articles")
}

data class WellnessItem(
    val id: String,
    val category: WellnessCategory,
    val title: String,
    val summary: String,
    val body: String,
    val durationMinutes: Int? = null,
    val steps: List<String> = emptyList(),
    // Paces the breathing guide on the detail screen; null for non-breathing items.
    val breathPattern: BreathPattern? = null
)

// Seconds for each phase of one breath; a hold of 0 skips that hold.
data class BreathPattern(
    val inhale: Int,
    val holdIn: Int = 0,
    val exhale: Int,
    val holdOut: Int = 0
)

// Static, curated, read-only content -- no backend needed. Ships with app
// releases and changes rarely, so it's hardcoded here rather than fetched.
object WellnessLibrary {
    val items: List<WellnessItem> = listOf(
        WellnessItem(
            id = "box-breathing",
            category = WellnessCategory.BREATHING,
            title = "Box Breathing",
            summary = "A steady 4-4-4-4 rhythm to calm your nervous system.",
            body = "Box breathing is a simple technique used by athletes, first responders, and " +
                "therapists to quickly settle a racing mind and body. The equal-length pattern " +
                "gives your breathing something steady to follow, which can interrupt a spiral of " +
                "anxious thoughts. It's safe to do anywhere -- sitting at your desk, before a hard " +
                "conversation, or lying down before sleep.",
            durationMinutes = 4,
            steps = listOf(
                "Breathe in slowly through your nose for 4 counts.",
                "Hold your breath gently for 4 counts.",
                "Breathe out slowly through your mouth for 4 counts.",
                "Hold again for 4 counts, then repeat for a few rounds."
            ),
            breathPattern = BreathPattern(inhale = 4, holdIn = 4, exhale = 4, holdOut = 4)
        ),
        WellnessItem(
            id = "478-breathing",
            category = WellnessCategory.BREATHING,
            title = "4-7-8 Breathing",
            summary = "A longer exhale to help you wind down before sleep or a stressful moment.",
            body = "This pattern stretches out the exhale, which naturally signals your body to relax. " +
                "It's often used as a wind-down exercise before sleep, but it also works in the middle " +
                "of a stressful moment when you need a quick reset. If 7 or 8 counts feels too long at " +
                "first, shorten the pattern proportionally (e.g. 2-3.5-4) and build up over time.",
            durationMinutes = 3,
            steps = listOf(
                "Breathe in quietly through your nose for 4 counts.",
                "Hold your breath for 7 counts.",
                "Exhale slowly and completely through your mouth for 8 counts.",
                "Repeat the cycle 3-4 times."
            ),
            breathPattern = BreathPattern(inhale = 4, holdIn = 7, exhale = 8)
        ),
        WellnessItem(
            id = "belly-breathing",
            category = WellnessCategory.BREATHING,
            title = "Belly Breathing",
            summary = "Slow, deep breaths from your diaphragm instead of your chest.",
            body = "Many of us breathe shallowly from the chest when stressed, which can actually keep " +
                "the body in a heightened state. Belly breathing brings the breath deeper, activating " +
                "your body's natural relaxation response. It takes a little practice to feel natural, " +
                "so don't worry if it feels awkward at first.",
            durationMinutes = 5,
            steps = listOf(
                "Place one hand on your chest and one on your belly.",
                "Breathe in slowly through your nose, letting your belly (not your chest) rise.",
                "Breathe out slowly through pursed lips, feeling your belly fall.",
                "Continue for several minutes, keeping the breath slow and even."
            ),
            breathPattern = BreathPattern(inhale = 5, exhale = 5)
        ),
        WellnessItem(
            id = "grounding-54321",
            category = WellnessCategory.COPING_TIPS,
            title = "5-4-3-2-1 Grounding",
            summary = "Use your senses to anchor yourself in the present moment.",
            body = "When anxiety or overwhelming thoughts take over, this exercise gently pulls your " +
                "attention back to the present by engaging your five senses one at a time. It's " +
                "especially useful when your mind is racing or replaying something and you need a " +
                "concrete way to interrupt that loop.",
            steps = listOf(
                "Name 5 things you can see around you.",
                "Name 4 things you can physically feel or touch.",
                "Name 3 things you can hear right now.",
                "Name 2 things you can smell.",
                "Name 1 thing you can taste."
            )
        ),
        WellnessItem(
            id = "reframe-thought",
            category = WellnessCategory.COPING_TIPS,
            title = "Reframe a Difficult Thought",
            summary = "A simple way to gently question thoughts that feel overwhelming.",
            body = "Our minds sometimes present thoughts as absolute facts, even when they're really " +
                "just one interpretation among several. This isn't about forcing positivity -- it's " +
                "about giving yourself a fairer, more balanced look at the situation before deciding " +
                "how much weight to give a thought.",
            steps = listOf(
                "Write down the thought exactly as it appeared in your mind.",
                "Ask: what evidence actually supports this thought? What evidence doesn't?",
                "Ask: what would I say to a friend who had this exact thought?",
                "Write a more balanced version of the thought based on your answers."
            )
        ),
        WellnessItem(
            id = "self-compassion-break",
            category = WellnessCategory.COPING_TIPS,
            title = "Self-Compassion Break",
            summary = "Three short phrases to be kinder to yourself in a hard moment.",
            body = "It's common to be far harsher with ourselves than we'd ever be with someone we care " +
                "about. This short exercise, adapted from self-compassion research, offers a quick way " +
                "to acknowledge difficulty without judgment and respond to yourself with the same " +
                "kindness you'd offer a friend.",
            steps = listOf(
                "Acknowledge the moment: \"This is a hard moment for me.\"",
                "Remember you're not alone: \"Difficulty is something everyone experiences.\"",
                "Offer yourself kindness: \"May I be kind to myself right now.\""
            )
        ),
        WellnessItem(
            id = "understanding-anxiety",
            category = WellnessCategory.ARTICLES,
            title = "Understanding Anxiety",
            summary = "What anxiety is, why it happens, and when it may be worth extra support.",
            body = "Anxiety is your body's natural alarm system -- it's designed to protect you from " +
                "danger by triggering physical changes like a faster heartbeat, tense muscles, or " +
                "racing thoughts. In short bursts, this response is normal and even helpful. It becomes " +
                "harder to live with when it shows up often, feels disproportionate to what's actually " +
                "happening, or starts getting in the way of daily life -- work, relationships, sleep, or " +
                "things you used to enjoy.\n\n" +
                "There's no single cause: genetics, life experiences, ongoing stress, and physical " +
                "health can all play a role, and it's rarely just one thing. If anxiety is a frequent, " +
                "heavy presence in your life rather than an occasional visitor, that's a reasonable " +
                "signal to talk to a mental health professional -- not a sign that something is " +
                "\"wrong\" with you, just that extra support could genuinely help."
        ),
        WellnessItem(
            id = "building-a-sleep-routine",
            category = WellnessCategory.ARTICLES,
            title = "Building a Better Sleep Routine",
            summary = "Small, realistic changes that can improve how well you sleep.",
            body = "Sleep and mental health influence each other in both directions -- poor sleep can " +
                "worsen low mood and anxiety, and low mood or anxiety can make it harder to sleep well. " +
                "You don't need a complete overhaul to see a difference; a few consistent habits often " +
                "matter more than any single big change.\n\n" +
                "Try going to bed and waking up at roughly the same time each day, even on weekends. " +
                "Dim lights and step away from screens for the last 30-60 minutes before bed if you " +
                "can. Keep your bedroom cool, dark, and reserved mainly for sleep rather than scrolling " +
                "or working. And if you find yourself lying awake worrying, it's okay to get up, do " +
                "something calming in low light, and go back to bed once you feel sleepy again rather " +
                "than forcing it."
        ),
        WellnessItem(
            id = "talking-to-someone",
            category = WellnessCategory.ARTICLES,
            title = "When to Talk to Someone",
            summary = "Signs that it might help to reach out to a professional or someone you trust.",
            body = "It can be hard to know when something is \"serious enough\" to bring up with someone " +
                "else. A helpful way to think about it: if what you're feeling is affecting your sleep, " +
                "appetite, relationships, or ability to get through your day for more than a couple of " +
                "weeks, that's worth talking about -- regardless of how it compares to what anyone else " +
                "might be going through.\n\n" +
                "You don't need to wait for a crisis to reach out. Talking to a trusted friend, family " +
                "member, or a mental health professional early on can make things easier to carry, and " +
                "it often prevents smaller struggles from growing heavier. If you're ever having " +
                "thoughts of harming yourself, please use GabAI's Emergency Support option or a crisis " +
                "hotline right away -- you don't have to go through that alone."
        )
    )

    fun findById(id: String): WellnessItem? = items.find { it.id == id }

    // What to suggest for each emotion the backend tags in a message, best fit
    // first. Same lists as gabai-web's EMOTION_SUGGESTIONS -- keep both in step.
    private val emotionSuggestions = mapOf(
        Emotion.ANXIETY to listOf("box-breathing", "grounding-54321", "understanding-anxiety"),
        Emotion.FEAR to listOf("grounding-54321", "478-breathing", "box-breathing"),
        Emotion.SADNESS to listOf("self-compassion-break", "belly-breathing", "talking-to-someone"),
        Emotion.DEPRESSION to listOf("talking-to-someone", "self-compassion-break", "reframe-thought")
    )

    // Suggestions for the given emotions, strongest emotion first, without repeats.
    fun suggestionsFor(emotions: List<Emotion>, limit: Int = 3): List<WellnessItem> =
        emotions.flatMap { emotionSuggestions[it].orEmpty() }
            .distinct()
            .mapNotNull(::findById)
            .take(limit)
}

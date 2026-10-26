package com.gabai.data.remote.api

/**
 * Picks the language for the app's two canned replies (the offline keyword
 * fail-safe and the backend-unreachable fallback), which never reach the AI
 * and so can't follow its reply-in-the-user's-language rule. Mirrors
 * gabai-backend's src/modules/chat/replyLanguage.ts -- keep the word list and
 * both replies in sync.
 *
 * A deliberately simple heuristic: common Filipino function words that rarely
 * appear in English. Taglish counts as Filipino.
 */
object ReplyLanguage {
    private val filipinoMarkers = setOf(
        "ako", "ko", "ka", "mo", "siya", "kami", "tayo", "kayo", "sila", "niya", "natin", "namin",
        "ang", "ng", "nang", "mga", "sa", "kay", "yung", "ung", "ito", "iyan", "yan", "dito", "diyan",
        "hindi", "di", "wala", "walang", "may", "meron", "po", "opo", "oo", "naman", "lang", "lamang",
        "ba", "pa", "na", "din", "rin", "daw", "raw", "kasi", "talaga", "sobra", "sobrang", "parang",
        "gusto", "ayoko", "ayaw", "kailangan", "pwede", "puwede", "bakit", "paano", "ano", "sino",
        "kumusta", "kamusta", "salamat", "nalulungkot", "malungkot", "pagod", "takot", "galit",
        "nararamdaman", "pakiramdam", "buhay", "mamatay", "magpakamatay"
    )

    private val wordPattern = Regex("[a-zñ']+")

    fun looksFilipino(text: String): Boolean {
        val words = wordPattern.findAll(text.lowercase()).map { it.value }.toList()
        val hits = words.count { it in filipinoMarkers }
        // One marker is enough for a very short message; longer ones need two
        // so a stray "na"/"sa" in English text doesn't flip the language.
        return if (words.size <= 3) hits >= 1 else hits >= 2
    }

    val severeFallbackReply = LocalizedReply(
        en = "I'm really concerned about what you're sharing. Your safety matters deeply. " +
            "Please reach out to a professional who can support you through this. " +
            "You don't have to go through this alone.",
        fil = "Nag-aalala talaga ako sa ibinabahagi mo. Mahalaga ang kaligtasan mo. " +
            "Pakiusap, lumapit ka sa isang propesyonal na makakatulong sa iyo. " +
            "Hindi mo kailangang harapin ito nang mag-isa."
    )

    val networkFallbackReply = LocalizedReply(
        en = "Thank you for sharing that with me. I'm here to listen and support you. " +
            "Can you tell me more about how you're feeling?",
        fil = "Salamat sa pagbabahagi mo sa akin. Nandito ako para makinig at sumuporta sa iyo. " +
            "Puwede mo bang ikuwento pa kung ano ang nararamdaman mo?"
    )

    fun inLanguageOf(userInput: String, reply: LocalizedReply): String =
        if (looksFilipino(userInput)) reply.fil else reply.en
}

data class LocalizedReply(val en: String, val fil: String)

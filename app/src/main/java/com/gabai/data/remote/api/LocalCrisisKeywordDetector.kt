package com.gabai.data.remote.api

/**
 * Offline half of the two-tier keyword fail-safe. Mirrors gabai-backend's
 * src/modules/chat/localCrisisKeywordDetector.ts and gabai-web's
 * lib/localCrisisKeywordDetector.ts exactly -- keep all three lists in sync.
 *
 * Plain substring matching, so every entry is a phrase specific enough to mean
 * a crisis on its own. Single words only where the root itself means suicide
 * ("pakamatay", "hikog"); a bare "mamatay" would also match everyday phrases
 * like "mamatay sa kakatawa". Over-matching is the accepted direction.
 */
internal object LocalCrisisKeywordDetector {
    private val severeKeywords = listOf(
        // English
        "suicide", "want to die", "kill myself", "end my life",
        "want to end it", "no reason to live", "better off dead",

        // Filipino / Taglish
        "pakamatay", // magpakamatay, magpapakamatay, nagpakamatay, pagpapakamatay
        "gusto ko nang mamatay", "gusto ko na mamatay", "gusto ko ng mamatay", "gusto kong mamatay",
        "sana mamatay na ako", "sana patay na ako",
        "ayoko nang mabuhay", "ayoko na mabuhay", "ayoko ng mabuhay",
        "ayaw ko nang mabuhay", "ayaw ko na mabuhay",
        "walang dahilan para mabuhay", "wala nang dahilan para mabuhay", "wala ng dahilan para mabuhay",
        "mas mabuti pang mamatay", "mas mabuti pang patay", "mas mabuti kung wala na ako",
        "tapusin ang buhay ko", "tapusin ko na ang buhay ko", "tatapusin ko na ang buhay ko",
        "wakasan ang buhay ko",
        "saktan ang sarili", "sasaktan ko ang sarili", "sinasaktan ko ang sarili",
        "maglaslas", "naglaslas", "naglalaslas", "maglalaslas",
        "uminom ng lason", "iinom ng lason",

        // Cebuano / Bisaya
        "hikog", // maghikog, naghikog, paghikog
        "gusto na ko mamatay", "gusto nako mamatay"
    )

    private val whitespace = Regex("\\s+")

    fun detect(userInput: String): Boolean {
        // Collapsing runs of whitespace lets "gusto  ko na\nmamatay" still match.
        val normalized = userInput.lowercase().replace(whitespace, " ")
        return severeKeywords.any { normalized.contains(it) }
    }
}

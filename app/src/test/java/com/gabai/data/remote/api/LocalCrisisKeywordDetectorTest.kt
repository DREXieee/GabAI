package com.gabai.data.remote.api

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * This is the network-independent fail-safe: it must catch a severe crisis
 * phrase even when the backend is unreachable. Every phrase in
 * [LocalCrisisKeywordDetector]'s list gets its own case so a future edit that
 * silently drops one doesn't slip through unnoticed.
 */
class LocalCrisisKeywordDetectorTest {

    @Test
    fun `detects each configured severe phrase on its own`() {
        val phrases = listOf(
            "suicide",
            "want to die",
            "kill myself",
            "end my life",
            "want to end it",
            "no reason to live",
            "better off dead"
        )
        phrases.forEach { phrase ->
            assertTrue("expected \"$phrase\" to be detected", LocalCrisisKeywordDetector.detect(phrase))
        }
    }

    @Test
    fun `detects a severe phrase embedded in a longer sentence`() {
        assertTrue(LocalCrisisKeywordDetector.detect("i think about suicide every day and i don't know what to do"))
        assertTrue(LocalCrisisKeywordDetector.detect("everyone would be better off dead if i were gone"))
    }

    @Test
    fun `does not flag ordinary distress that isn't a severe crisis phrase`() {
        assertFalse(LocalCrisisKeywordDetector.detect("i had a really rough day at work today"))
        assertFalse(LocalCrisisKeywordDetector.detect("i'm feeling anxious about my exam tomorrow"))
        assertFalse(LocalCrisisKeywordDetector.detect("i feel so hopeless about finishing this project on time"))
    }

    @Test
    fun `blank input is never flagged`() {
        assertFalse(LocalCrisisKeywordDetector.detect(""))
        assertFalse(LocalCrisisKeywordDetector.detect("   "))
    }

    @Test
    fun `known tradeoff - hyperbolic phrasing containing a severe phrase still triggers`() {
        // Naive substring matching means "kill myself laughing" also matches.
        // That's accepted here: for a crisis fail-safe, an over-eager true
        // positive (patient sees a supportive message + hotline card they
        // didn't strictly need) is a far cheaper failure mode than a missed
        // true crisis, so this is documented as intentional, not a bug.
        assertTrue(LocalCrisisKeywordDetector.detect("that joke was so funny i could kill myself laughing"))
    }
}

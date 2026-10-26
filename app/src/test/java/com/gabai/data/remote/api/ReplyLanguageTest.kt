package com.gabai.data.remote.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Same cases as gabai-backend's replyLanguage.test.ts. */
class ReplyLanguageTest {

    @Test
    fun detectsFilipinoAndTaglish() {
        listOf(
            "Pagod na ako",
            "Salamat",
            "Hindi ko na alam ang gagawin ko",
            "Sobrang stressed ako sa work ngayon",
            "Gusto ko lang may makausap",
            "Kumusta ka?"
        ).forEach { assertTrue("expected \"$it\" to read as Filipino", ReplyLanguage.looksFilipino(it)) }
    }

    @Test
    fun keepsEnglishInEnglish() {
        listOf(
            "I feel hopeless and I don't want to live anymore.",
            "Work has been really stressful this week",
            "Hi",
            "I could use someone to talk to today",
            "My sister lives in Manila and she said I should rest"
        ).forEach { assertFalse("expected \"$it\" to read as English", ReplyLanguage.looksFilipino(it)) }
    }

    @Test
    fun picksMatchingCannedReply() {
        assertEquals(
            ReplyLanguage.networkFallbackReply.fil,
            ReplyLanguage.inLanguageOf("Pagod na ako", ReplyLanguage.networkFallbackReply)
        )
        assertEquals(
            ReplyLanguage.networkFallbackReply.en,
            ReplyLanguage.inLanguageOf("I'm so tired", ReplyLanguage.networkFallbackReply)
        )
    }
}

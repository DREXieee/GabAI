package com.gabai.data.remote.api

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * This is the network-independent fail-safe: it must catch a severe crisis
 * phrase even when the backend is unreachable. Every phrase in
 * [LocalCrisisKeywordDetector]'s list gets its own case so a future edit that
 * silently drops one doesn't slip through unnoticed. Same cases as
 * gabai-backend's localCrisisKeywordDetector.test.ts.
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
            "better off dead",
            "pakamatay",
            "gusto ko nang mamatay",
            "gusto ko na mamatay",
            "gusto ko ng mamatay",
            "gusto kong mamatay",
            "sana mamatay na ako",
            "sana patay na ako",
            "ayoko nang mabuhay",
            "ayoko na mabuhay",
            "ayoko ng mabuhay",
            "ayaw ko nang mabuhay",
            "ayaw ko na mabuhay",
            "walang dahilan para mabuhay",
            "wala nang dahilan para mabuhay",
            "wala ng dahilan para mabuhay",
            "mas mabuti pang mamatay",
            "mas mabuti pang patay",
            "mas mabuti kung wala na ako",
            "tapusin ang buhay ko",
            "tapusin ko na ang buhay ko",
            "tatapusin ko na ang buhay ko",
            "wakasan ang buhay ko",
            "saktan ang sarili",
            "sasaktan ko ang sarili",
            "sinasaktan ko ang sarili",
            "maglaslas",
            "naglaslas",
            "naglalaslas",
            "maglalaslas",
            "uminom ng lason",
            "iinom ng lason",
            "hikog",
            "gusto na ko mamatay",
            "gusto nako mamatay"
        )
        phrases.forEach { phrase ->
            assertTrue("expected \"$phrase\" to be detected", LocalCrisisKeywordDetector.detect(phrase))
        }
    }

    @Test
    fun `detects a severe phrase embedded in a longer sentence, case-insensitively`() {
        assertTrue(LocalCrisisKeywordDetector.detect("I think about SUICIDE every day and I don't know what to do"))
        assertTrue(LocalCrisisKeywordDetector.detect("Everyone would be Better Off Dead if I were gone"))
    }

    @Test
    fun `detects Filipino, Taglish and Cebuano crisis messages in real sentences`() {
        listOf(
            "Gusto ko nang mamatay, pagod na pagod na ako",
            "Magpapakamatay na lang ako",
            "Iniisip kong magpakamatay mamayang gabi",
            "Ayoko na mabuhay, wala namang nagmamahal sa akin",
            "sobrang hirap na, gusto kong mamatay",
            "Naglalaslas ako kapag sobrang bigat ng nararamdaman ko",
            "Mas mabuti pang mamatay na lang ako kaysa ganito",
            "Gusto na ko mamatay",
            "Maghikog na lang ko"
        ).forEach { assertTrue("expected \"$it\" to be detected", LocalCrisisKeywordDetector.detect(it)) }
    }

    @Test
    fun `matches across extra spaces and line breaks`() {
        assertTrue(LocalCrisisKeywordDetector.detect("gusto  ko   na\nmamatay"))
    }

    @Test
    fun `does not flag ordinary distress that isn't a severe crisis phrase`() {
        assertFalse(LocalCrisisKeywordDetector.detect("i had a really rough day at work today"))
        assertFalse(LocalCrisisKeywordDetector.detect("i'm feeling anxious about my exam tomorrow"))
        assertFalse(LocalCrisisKeywordDetector.detect("i feel so hopeless about finishing this project on time"))
    }

    @Test
    fun `does not flag everyday Filipino phrases that use mamatay, patay or buhay`() {
        listOf(
            "Pagod na pagod ako sa trabaho ngayong linggo",
            "Halos mamatay ako sa kakatawa kanina",
            "Patay na yung cellphone ko",
            "Ang hirap ng buhay ngayon pero kakayanin ko",
            "May dahilan pa ako para mabuhay, ang pamilya ko",
            "Malungkot ako kasi namatay yung aso namin"
        ).forEach { assertFalse("expected \"$it\" not to be flagged", LocalCrisisKeywordDetector.detect(it)) }
    }

    @Test
    fun `blank input is never flagged`() {
        assertFalse(LocalCrisisKeywordDetector.detect(""))
        assertFalse(LocalCrisisKeywordDetector.detect("   "))
    }

    @Test
    fun `known tradeoff - hyperbolic phrasing containing a severe phrase still triggers`() {
        // Naive substring matching means "kill myself laughing" also matches,
        // and so does the Filipino idiom for overworking ("nagpapakamatay sa
        // trabaho"). For a crisis fail-safe, an over-eager true positive
        // (patient sees a supportive message + hotline card they didn't
        // strictly need) is a far cheaper failure mode than a missed true
        // crisis, so this is documented as intentional, not a bug.
        assertTrue(LocalCrisisKeywordDetector.detect("that joke was so funny i could kill myself laughing"))
        assertTrue(LocalCrisisKeywordDetector.detect("Nagpapakamatay ako sa trabaho para sa pamilya ko"))
    }
}

package com.gabai.screenshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.airbnb.lottie.LottieCompositionFactory
import com.gabai.R
import com.gabai.domain.model.BreathPattern
import com.gabai.domain.model.ChatMessage
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAITheme
import com.gabai.presentation.ui.chatbot.ChatBubble
import com.gabai.presentation.ui.common.CrisisActionBar
import com.gabai.presentation.ui.common.SafetyResourcesCard
import com.gabai.presentation.ui.common.SkeletonList
import com.gabai.domain.model.Emotion
import com.gabai.presentation.ui.wellness.BreathingGuide
import com.gabai.presentation.ui.wellness.WellnessSuggestionCard
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot tests (Roborazzi on Robolectric) for self-contained pieces of the
 * UI, each in light and dark. `./gradlew recordRoborazziDebug` writes the
 * reference images to app/src/test/screenshots; `verifyRoborazziDebug` fails
 * when one changes. Screens that load data aren't covered: they need the backend.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class ComponentScreenshotTest(private val darkTheme: Boolean) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "dark={0}")
        fun themes(): List<Array<Any>> = listOf(arrayOf(false), arrayOf(true))
    }

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun safetyResourcesCard() = capture("safety_resources_card") {
        SafetyResourcesCard()
    }

    @Test
    fun crisisActionBar() = capture("crisis_action_bar") {
        CrisisActionBar(onSpeakToProfessional = {}, onShowHotlines = {})
    }

    // At 200% font the buttons must wrap between buttons, never inside a label.
    @Test
    fun crisisActionBarLargeFont() = capture("crisis_action_bar_large_font") {
        LargeFont { CrisisActionBar(onSpeakToProfessional = {}, onShowHotlines = {}) }
    }

    // The narrowest common phone width, where 200% font forces "Hotlines" down a row.
    @Test
    @Config(qualifiers = "w360dp-h800dp-xhdpi")
    fun crisisActionBarLargeFontNarrow() = capture("crisis_action_bar_large_font_360dp") {
        LargeFont { CrisisActionBar(onSpeakToProfessional = {}, onShowHotlines = {}) }
    }

    @Composable
    private fun LargeFont(content: @Composable () -> Unit) {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f), content = content)
    }

    @Test
    fun chatBubbles() = capture("chat_bubbles") {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)) {
            ChatBubble(ChatMessage(id = "1", text = "Explain the 5-4-3-2-1 grounding technique.", isUser = true))
            ChatBubble(
                ChatMessage(
                    id = "2",
                    text = "Here it is, step by step:\n" +
                        "- **5 things you can see:** name them slowly.\n" +
                        "- **4 things you can touch:** notice each texture.\n" +
                        "- **3 things you can hear:** near or far.",
                    isUser = false
                )
            )
        }
    }

    @Test
    fun wellnessSuggestionCard() = capture("wellness_suggestion_card") {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)) {
            WellnessSuggestionCard(emotion = Emotion.ANXIETY, onOpen = {}, onBookConsultation = {})
            WellnessSuggestionCard(emotion = Emotion.DEPRESSION, onOpen = {}, onBookConsultation = {})
        }
    }

    @Test
    fun skeletonList() = capture("skeleton_list") {
        SkeletonList(loadingDescription = "Loading history", rowCount = 3)
    }

    @Test
    fun breathingGuide() {
        // Lottie parses compositions off the main thread; parse it into the
        // cache up front so the capture can't race the load and record no orb.
        LottieCompositionFactory.fromRawResSync(RuntimeEnvironment.getApplication(), R.raw.breathing_orb)
        capture("breathing_guide") {
            BreathingGuide(
                pattern = BreathPattern(inhale = 4, holdIn = 4, exhale = 4, holdOut = 4),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    private fun capture(name: String, content: @Composable () -> Unit) {
        // Frozen clock: shimmer and other infinite animations would otherwise
        // never let the rule go idle, and every capture would differ.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            GabAITheme(darkTheme = darkTheme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(modifier = Modifier.padding(Dimens.SpaceM)) { content() }
                }
            }
        }
        // Let posted work (e.g. a loaded Lottie composition) land, then draw it.
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeByFrame()
        val theme = if (darkTheme) "dark" else "light"
        composeRule.onRoot().captureRoboImage("src/test/screenshots/${name}_$theme.png")
    }
}

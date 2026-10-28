package com.gabai.presentation.ui.wellness

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.gabai.R
import com.gabai.domain.model.BreathPattern
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.util.rememberReducedMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private enum class BreathPhase(val label: String) {
    IN("Breathe in"),
    HOLD("Hold"),
    OUT("Breathe out")
}

// Where the orb sits while paused, or for good with animations off.
private const val RestingBreath = 0.35f

// The counts are the exercise, so they run on the wall clock: Compose would
// otherwise stretch or squeeze each tween by the system animator scale.
private val RealTimeMotion = object : MotionDurationScale {
    override val scaleFactor: Float get() = 1f
}

/**
 * Paced breathing orb (res/raw/breathing_orb.json, tinted from the theme): it
 * grows on the in-breath, rests on holds and shrinks on the out-breath at the
 * exercise's own counts. It only runs after the user taps Start; with system
 * animations off it stays a still orb beside the written steps and never runs
 * on a timer (see the motion-and-reduced-motion skill).
 */
@Composable
fun BreathingGuide(pattern: BreathPattern, modifier: Modifier = Modifier) {
    val reducedMotion = rememberReducedMotion()
    var running by rememberSaveable { mutableStateOf(false) }
    var phase by remember { mutableStateOf<BreathPhase?>(null) }
    var phaseSeconds by remember { mutableIntStateOf(0) }
    val breath = remember { Animatable(RestingBreath) }

    LaunchedEffect(running, reducedMotion, pattern) {
        phase = null
        if (reducedMotion) running = false
        if (!running) {
            breath.snapTo(RestingBreath)
            return@LaunchedEffect
        }
        withContext(RealTimeMotion) {
            while (true) {
                phase = BreathPhase.IN
                phaseSeconds = pattern.inhale
                breath.animateTo(1f, tween(pattern.inhale * 1000, easing = EaseInOutSine))
                if (pattern.holdIn > 0) {
                    phase = BreathPhase.HOLD
                    phaseSeconds = pattern.holdIn
                    delay(pattern.holdIn * 1000L)
                }
                phase = BreathPhase.OUT
                phaseSeconds = pattern.exhale
                breath.animateTo(0f, tween(pattern.exhale * 1000, easing = EaseInOutSine))
                if (pattern.holdOut > 0) {
                    phase = BreathPhase.HOLD
                    phaseSeconds = pattern.holdOut
                    delay(pattern.holdOut * 1000L)
                }
            }
        }
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.breathing_orb))
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.COLOR, MaterialTheme.colorScheme.tertiary.toArgb(), "**")
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
    ) {
        LottieAnimation(
            composition = composition,
            progress = { breath.value },
            dynamicProperties = dynamicProperties,
            modifier = Modifier.size(Dimens.BreathingOrbSize)
        )
        Text(
            text = phase?.let { "${it.label} for $phaseSeconds" } ?: pattern.summary(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        if (reducedMotion) {
            Text(
                text = "Follow the steps below at your own pace.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            OutlinedButton(onClick = { running = !running }) {
                Text(if (running) "Pause" else "Start breathing")
            }
        }
    }
}

private fun BreathPattern.summary(): String = listOfNotNull(
    "In $inhale",
    holdIn.takeIf { it > 0 }?.let { "Hold $it" },
    "Out $exhale",
    holdOut.takeIf { it > 0 }?.let { "Hold $it" }
).joinToString(" · ")

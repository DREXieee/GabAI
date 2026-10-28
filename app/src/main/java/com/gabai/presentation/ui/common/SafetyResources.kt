package com.gabai.presentation.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.gabai.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.gabai.presentation.theme.Dimens

// Cross-referenced 2026-07-27 against the official In Touch site (in-touch.org),
// WHO's coverage of the DOH-run Hopeline project, and NCMH-listing aggregators
// (findahelpline.com, mentalhealthph.org) -- all converged on these numbers.
// This is desk research, not a live call to each line; do a phone spot-check
// before relying on this for real users, and recheck periodically since crisis
// lines do change numbers/carriers over time.
private class Hotline(val name: String, val numbers: List<String>)

private val HOTLINES = listOf(
    Hotline("NCMH Crisis Hotline", listOf("1553", "0917-899-8727", "0966-351-4518", "0919-057-1553")),
    Hotline("Hopeline PH (DOH)", listOf("(02) 8804-4673", "0917-558-4673", "0918-873-4673")),
    Hotline("In Touch Crisis Line", listOf("(02) 8893-7603", "0917-800-1123"))
)

// ACTION_DIAL, not ACTION_CALL: it opens the phone app with the number already
// filled in (one tap to place the call) and needs no CALL_PHONE permission,
// so it can never be blocked by a denied permission in an emergency.
private fun dialIntent(number: String): Intent =
    Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number.filter { it.isDigit() || it == '+' }))

private const val DISCLAIMER_COPY =
    "GabAI is a supportive companion and is not a substitute for professional " +
        "diagnosis, therapy, or emergency medical care."

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SafetyResourcesCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val onContainer = MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
        ) {
            Text(
                text = "If you are in immediate danger, call 911 now.",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = onContainer
            )
            Button(
                onClick = { context.startActivity(dialIntent("911")) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(painterResource(R.drawable.ic_lucide_phone), contentDescription = null)
                Text("  Call 911", fontWeight = FontWeight.SemiBold)
            }

            Text(
                text = "Philippines crisis and mental health hotlines (tap a number to call):",
                style = MaterialTheme.typography.bodySmall,
                color = onContainer
            )
            HOTLINES.forEach { hotline ->
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS)) {
                    Text(
                        text = hotline.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = onContainer
                    )
                    // Two equal columns so the buttons form a tidy grid instead of
                    // wrapping raggedly; a spacer fills the last slot of an odd count.
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS),
                        maxItemsInEachRow = 2
                    ) {
                        hotline.numbers.forEach { number ->
                            AssistChip(
                                onClick = { context.startActivity(dialIntent(number)) },
                                modifier = Modifier.weight(1f),
                                label = { Text(number, maxLines = 1, softWrap = false) },
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.ic_lucide_phone),
                                        contentDescription = "Call ${hotline.name}",
                                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    leadingIconContentColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                        if (hotline.numbers.size % 2 == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Text(
                text = DISCLAIMER_COPY,
                style = MaterialTheme.typography.bodySmall,
                color = onContainer
            )
        }
    }
}

// Pinned above the chat input once a conversation is SEVERE. Kept compact so
// the conversation stays readable: 911 and a professional are always one tap
// away, and the full hotline list (SafetyResourcesCard) opens from "Hotlines".
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrisisActionBar(
    onSpeakToProfessional: () -> Unit,
    onShowHotlines: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val urgentColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        // Wraps instead of clipping at large font sizes. "Call 911" takes whatever
        // "Hotlines" leaves, so it's the biggest target. FlowRow checks whether a
        // weighted button fits from its minimum intrinsic width, which for Text is
        // only its longest word; IntrinsicSize.Max reports the whole label, so the
        // row breaks before "Call 911" would have to split across two lines.
        FlowRow(
            modifier = Modifier.padding(Dimens.SpaceS),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
        ) {
            Button(
                onClick = { context.startActivity(dialIntent("911")) },
                modifier = Modifier.weight(1f),
                colors = urgentColors
            ) {
                Icon(painterResource(R.drawable.ic_lucide_phone), contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Call 911", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(IntrinsicSize.Max))
            }
            // Same surface-on-red treatment as the hotline number chips.
            Button(
                onClick = onShowHotlines,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Hotlines", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onSpeakToProfessional,
                modifier = Modifier.fillMaxWidth(),
                colors = urgentColors
            ) {
                Text("Speak to a Professional", textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun SafetyDisclaimerLine(modifier: Modifier = Modifier) {
    Text(
        text = DISCLAIMER_COPY,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

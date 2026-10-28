package com.gabai.presentation.ui.wellness

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gabai.R
import com.gabai.domain.model.Emotion
import com.gabai.domain.model.WellnessLibrary
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAIShapes
import com.gabai.presentation.theme.WindowWidthSize
import com.gabai.presentation.theme.rememberWindowWidthSize

/**
 * "Suggested for you" under GabAI's latest reply: the best-fit Wellness item for
 * the emotion the backend read in the patient's message. Depression also gets a
 * way to book a professional, since self-help alone isn't the right answer there.
 */
@Composable
fun WellnessSuggestionCard(
    emotion: Emotion,
    onOpen: (itemId: String) -> Unit,
    onBookConsultation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val item = WellnessLibrary.suggestionsFor(listOf(emotion), limit = 1).firstOrNull() ?: return
    val accent = item.category.accentColor()
    val maxWidth = if (rememberWindowWidthSize() == WindowWidthSize.COMPACT)
        Dimens.ChatBubbleMaxWidthCompact
    else
        Dimens.ChatBubbleMaxWidthWide

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            onClick = { onOpen(item.id) },
            modifier = Modifier.widthIn(max = maxWidth),
            shape = GabAIShapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(Dimens.SpaceS),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.IconBadgeSize)
                        .background(accent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(item.category.iconRes()), contentDescription = null, tint = accent)
                }
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "Suggested for you",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.title + (item.durationMinutes?.let { " · $it min" } ?: ""),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (emotion == Emotion.DEPRESSION) {
            TextButton(onClick = onBookConsultation) {
                Text("Or book a consultation with a professional")
            }
        }
    }
}

package com.gabai.presentation.ui.common

import androidx.compose.foundation.layout.Arrangement
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
import com.gabai.presentation.theme.Dimens

// Cross-referenced 2026-07-27 against the official In Touch site (in-touch.org),
// WHO's coverage of the DOH-run Hopeline project, and NCMH-listing aggregators
// (findahelpline.com, mentalhealthph.org) -- all converged on these numbers.
// This is desk research, not a live call to each line; do a phone spot-check
// before relying on this for real users, and recheck periodically since crisis
// lines do change numbers/carriers over time.
private const val HOTLINE_COPY = "Philippines crisis and mental health hotlines:\n" +
    "• NCMH Crisis Hotline: 1553 (landline) / 0917-899-8727 & 0966-351-4518 (Globe/TM) / 0919-057-1553 (Smart/TNT)\n" +
    "• Hopeline PH (DOH): (02) 8804-4673 (landline) / 0917-558-4673 & 0918-873-4673 (mobile)\n" +
    "• In Touch Crisis Line: (02) 8893-7603 (landline) / 0917-800-1123 (mobile)"

private const val DISCLAIMER_COPY =
    "GabAI is a supportive companion and is not a substitute for professional " +
        "diagnosis, therapy, or emergency medical care."

@Composable
fun SafetyResourcesCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
        ) {
            Text(
                text = "If you are in immediate danger, call 911 now.",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = HOTLINE_COPY,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = DISCLAIMER_COPY,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
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

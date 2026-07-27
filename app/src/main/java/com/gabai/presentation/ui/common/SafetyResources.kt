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
import androidx.compose.ui.unit.dp

// TODO: verify these hotline numbers before release - not independently confirmed as current/accurate.
private const val HOTLINE_COPY = "Philippines crisis and mental health hotlines (VERIFY BEFORE RELEASE):\n" +
    "• NCMH Crisis Hotline: 1553 (landline) / 0966-351-4518 / 0917-899-8727\n" +
    "• Hopeline PH: (02) 8804-4673 / 0917-558-4673 / 0918-873-4673\n" +
    "• In Touch Crisis Line: (02) 8893-7603 / 0917-800-1123"

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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

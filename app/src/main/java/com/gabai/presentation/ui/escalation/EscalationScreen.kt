package com.gabai.presentation.ui.escalation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gabai.domain.model.CrisisLevel
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.ui.common.SafetyResourcesCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscalationScreen(
    crisisLevel: CrisisLevel,
    triggeringMessage: String,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val viewModel: EscalationViewModel = viewModel(factory = EscalationViewModelFactory(context))
    val confirmation by viewModel.confirmation.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Request Human Support") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = when (crisisLevel) {
                    CrisisLevel.SEVERE -> "You reached out during a difficult moment. You're not alone."
                    CrisisLevel.MODERATE -> "It sounds like things have been hard. Support is available."
                    CrisisLevel.NONE -> "You can request to speak with a mental health professional any time."
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            SafetyResourcesCard()

            val currentConfirmation = confirmation
            if (currentConfirmation == null) {
                Button(
                    onClick = { viewModel.requestHumanSupport(crisisLevel, triggeringMessage) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Request Human Support")
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Thank you for reaching out.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Staff will reach out within ${currentConfirmation.estimatedResponseMinutes} minutes.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Button(
                    onClick = {
                        navController?.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Return to Dashboard")
                }
            }
        }
    }
}

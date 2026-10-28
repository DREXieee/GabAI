package com.gabai.presentation.ui.consultation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gabai.domain.repository.ConsultationStatusUpdate
import com.gabai.domain.repository.RemoteConsultationStatus
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAIShapes
import com.gabai.presentation.theme.screenContentPadding
import com.gabai.presentation.util.formatFriendlyDate
import com.gabai.presentation.util.formatFriendlyTime
import com.gabai.presentation.ui.common.GabAILogoButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationDetailScreen(consultationId: String, navController: NavController? = null) {
    val context = LocalContext.current
    val viewModel: ConsultationDetailViewModel = viewModel(factory = ConsultationDetailViewModelFactory(context))

    val status by viewModel.status.collectAsState()
    val isCancelling by viewModel.isCancelling.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(consultationId) { viewModel.observe(consultationId) }

    LaunchedEffect(status) {
        val current = status
        // Also gate on CONFIRMED (mirrors EscalationScreen's IN_CALL check) --
        // without it, revisiting a since-COMPLETED/CANCELLED consultation whose
        // call was ended by the patient just tapping "Leave" in Daily's own UI
        // (never calling the backend's end-call endpoint, so the
        // ConsultationCallSession row never got endedAt set) would wrongly
        // redirect straight back into the stale call.
        if (current?.status == RemoteConsultationStatus.CONFIRMED &&
            current.dailyRoomUrl != null &&
            current.patientMeetingToken != null
        ) {
            navController?.navigate(Routes.callRoute(current.dailyRoomUrl, current.patientMeetingToken))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultation", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { GabAILogoButton(navController) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues)
        ) {
            when {
                status != null -> DetailContent(
                    status = status!!,
                    isCancelling = isCancelling,
                    onCancel = { viewModel.cancel(consultationId) },
                    onBookAnother = { navController?.navigate(Routes.CONSULTATION_BOOKING) }
                )

                error != null -> Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)

                else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun DetailContent(
    status: ConsultationStatusUpdate,
    isCancelling: Boolean,
    onCancel: () -> Unit,
    onBookAnother: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
        Card(
            shape = GabAIShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(Dimens.SpaceM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
            ) {
                Text(
                    text = statusLabel(status.status),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(status.doctorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${formatFriendlyDate(status.slotStartTime)} • ${formatFriendlyTime(status.slotStartTime)} - " +
                        formatFriendlyTime(status.slotEndTime),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!status.reasonNote.isNullOrBlank()) {
                    Text(
                        text = "\"${status.reasonNote}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        if (status.status == RemoteConsultationStatus.DECLINED) {
            Text(
                text = "${status.doctorName} couldn't take this time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = onBookAnother, modifier = Modifier.fillMaxWidth()) {
                Text("Choose Another Time")
            }
        }

        val isRequest = status.status == RemoteConsultationStatus.REQUESTED
        if (isRequest || status.status == RemoteConsultationStatus.CONFIRMED) {
            Text(
                text = if (isRequest) {
                    "Your request was sent. ${status.doctorName} will confirm it, and this screen updates on its own."
                } else {
                    "You'll be connected automatically here once your doctor starts the call."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onCancel,
                enabled = !isCancelling,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(if (isRequest) "Withdraw Request" else "Cancel Consultation")
                }
            }
        }
    }
}

private fun statusLabel(status: RemoteConsultationStatus): String = when (status) {
    RemoteConsultationStatus.REQUESTED -> "Waiting for confirmation"
    RemoteConsultationStatus.CONFIRMED -> "Confirmed"
    RemoteConsultationStatus.DECLINED -> "Declined"
    RemoteConsultationStatus.CANCELLED -> "Cancelled"
    RemoteConsultationStatus.COMPLETED -> "Completed"
}

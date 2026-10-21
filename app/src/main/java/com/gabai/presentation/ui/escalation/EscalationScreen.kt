package com.gabai.presentation.ui.escalation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gabai.domain.model.Clinic
import com.gabai.domain.model.CrisisLevel
import com.gabai.domain.repository.EscalationStatusUpdate
import com.gabai.domain.repository.RemoteEscalationStatus
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAIShapes
import com.gabai.presentation.theme.screenContentPadding
import com.gabai.presentation.ui.clinics.ClinicMap
import com.gabai.presentation.ui.clinics.clinicDirectionsIntent
import com.gabai.presentation.ui.common.GabAILogoButton
import com.gabai.presentation.ui.common.navigateToMenu
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gabai.presentation.ui.common.SafetyResourcesCard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import com.gabai.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscalationScreen(
    crisisLevel: CrisisLevel,
    escalationId: String?,
    sessionId: String?,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val viewModel: EscalationViewModel = viewModel(factory = EscalationViewModelFactory(context))

    val status by viewModel.status.collectAsState()
    val isRequesting by viewModel.isRequesting.collectAsState()
    val error by viewModel.error.collectAsState()
    val waitTimedOut by viewModel.waitTimedOut.collectAsState()

    LaunchedEffect(escalationId) {
        if (escalationId != null) {
            viewModel.observeExisting(escalationId)
        }
    }

    // A request that is still live: leaving the screen stops the status polling,
    // and the automatic jump into the staff call only happens from here, so the
    // patient is asked to confirm before leaving (logo button or system back).
    val requestActive = isRequesting ||
        (status != null && status?.status !in setOf(RemoteEscalationStatus.RESOLVED, RemoteEscalationStatus.ABANDONED))
    var pendingLeave by remember { mutableStateOf<(() -> Unit)?>(null) }
    val leaveToMenu = { navigateToMenu(navController) }

    // If the request ends or fails while the dialog is up, its warning is no
    // longer true -- close it rather than leave stale text on screen.
    LaunchedEffect(requestActive) {
        if (!requestActive) pendingLeave = null
    }

    BackHandler(enabled = requestActive) { pendingLeave = { navController?.popBackStack() } }

    pendingLeave?.let { leave ->
        AlertDialog(
            onDismissRequest = { pendingLeave = null },
            title = { Text("Leave this screen?") },
            text = {
                Text(
                    "Your request for a staff member is still active. If you leave now, you " +
                        "might miss their call. The hotline numbers stay available on this screen."
                )
            },
            confirmButton = {
                Button(onClick = { pendingLeave = null }) { Text("Stay") }
            },
            dismissButton = {
                TextButton(onClick = { pendingLeave = null; leave() }) { Text("Leave") }
            }
        )
    }

    // Reached from the dashboard's Emergency button: no chat session and no
    // existing escalation, so ask for a human straight away rather than
    // making someone in an emergency tap a second button.
    LaunchedEffect(escalationId, sessionId) {
        if (escalationId == null && sessionId == null) {
            viewModel.requestHumanSupport(null, crisisLevel)
        }
    }

    LaunchedEffect(status) {
        val current = status
        if (current != null &&
            current.status == RemoteEscalationStatus.IN_CALL &&
            current.dailyRoomUrl != null &&
            current.patientMeetingToken != null
        ) {
            navController?.navigate(Routes.callRoute(current.dailyRoomUrl, current.patientMeetingToken))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Human Support") },
                navigationIcon = {
                    GabAILogoButton(
                        navController,
                        onClick = { if (requestActive) pendingLeave = leaveToMenu else leaveToMenu() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            // Scrollable so the status area and hotline buttons stay reachable on
            // small screens or large font sizes; the top padding keeps the heading
            // from sitting flush against the app bar.
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(top = Dimens.SpaceM, bottom = Dimens.SpaceL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
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

            Text(
                text = "This starts our escalation process — connecting you with a real, trained person quickly.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedContent(
                targetState = status,
                transitionSpec = {
                    (fadeIn(tween(300, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(300)))
                        .togetherWith(fadeOut(tween(150)))
                        .using(SizeTransform(clip = false))
                },
                label = "escalationStatus"
            ) { currentStatus ->
                when {
                    currentStatus != null -> StatusCard(
                        status = currentStatus,
                        waitTimedOut = waitTimedOut,
                        onReturnToDashboard = {
                            navController?.navigate(Routes.DASHBOARD) {
                                // Chat is the root; drop the escalation screen above it.
                                popUpTo(Routes.CHATBOT) { inclusive = false }
                            }
                        }
                    )

                    error != null -> Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
                        Text(
                            text = error.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        // Only offered when nothing was ever created (a failed request),
                        // not after a dropped status poll.
                        if (status == null) {
                            Button(
                                onClick = { viewModel.requestHumanSupport(sessionId, crisisLevel) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Try again")
                            }
                        }
                    }

                    isRequesting -> RequestingIndicator()

                    sessionId != null -> Button(
                        onClick = { viewModel.requestHumanSupport(sessionId, crisisLevel) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Request Human Support")
                    }

                    // Dashboard path: the request is fired by the effect above; this
                    // covers the first frame before it starts.
                    else -> RequestingIndicator()
                }
            }

            // Below the request status, which is the active, time-sensitive part of
            // this screen; the 911 button and hotlines are still within the first
            // screenful.
            SafetyResourcesCard()
        }
    }
}

@Composable
private fun RequestingIndicator() {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressIndicator()
        Text("Requesting human support…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun StatusCard(
    status: EscalationStatusUpdate,
    waitTimedOut: Boolean,
    onReturnToDashboard: () -> Unit
) {
    val isTerminal = status.status == RemoteEscalationStatus.RESOLVED || status.status == RemoteEscalationStatus.ABANDONED

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
        Card(
            shape = GabAIShapes.large,
            colors = CardDefaults.cardColors(
                containerColor = if (isTerminal)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(Dimens.SpaceM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
            ) {
                Text(
                    text = statusTitle(status.status),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusDescription(status),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (status.status == RemoteEscalationStatus.PENDING && !isTerminal) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }
        }

        if (status.status == RemoteEscalationStatus.TRANSFERRED && status.transferredDoctorClinic != null) {
            TransferredClinicCard(clinic = status.transferredDoctorClinic)
        }

        if (waitTimedOut && status.status == RemoteEscalationStatus.PENDING) {
            Text(
                text = "We're taking longer than usual to reach a staff member. Please use the " +
                    "hotline numbers above to talk to someone right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (isTerminal) {
            Button(onClick = onReturnToDashboard, modifier = Modifier.fillMaxWidth()) {
                Text("Return to Dashboard")
            }
        }
    }
}

@Composable
private fun TransferredClinicCard(clinic: Clinic) {
    val context = LocalContext.current

    Card(shape = GabAIShapes.large) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
        ) {
            Text(
                text = clinic.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = clinic.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = GabAIShapes.medium
            ) {
                ClinicMap(clinics = listOf(clinic), userLocation = null, modifier = Modifier.fillMaxSize())
            }

            Button(
                onClick = { context.startActivity(clinicDirectionsIntent(clinic)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(painterResource(R.drawable.ic_lucide_footprints), contentDescription = null)
                Text(" Get Directions", modifier = Modifier.padding(start = Dimens.SpaceXXS))
            }
        }
    }
}

private fun statusTitle(status: RemoteEscalationStatus): String = when (status) {
    RemoteEscalationStatus.PENDING -> "Waiting for a staff member"
    RemoteEscalationStatus.CLAIMED -> "Staff connected"
    RemoteEscalationStatus.IN_CALL -> "In a call"
    RemoteEscalationStatus.TRANSFERRED -> "Referred to a doctor"
    RemoteEscalationStatus.RESOLVED -> "Thank you for reaching out"
    RemoteEscalationStatus.ABANDONED -> "Request closed"
}

private fun statusDescription(status: EscalationStatusUpdate): String = when (status.status) {
    RemoteEscalationStatus.PENDING -> "We've let our staff know. Someone will reach out shortly."
    RemoteEscalationStatus.CLAIMED -> "${status.claimedByStaffName ?: "A staff member"} has your request and is preparing to connect with you."
    RemoteEscalationStatus.IN_CALL -> "You're connected with ${status.claimedByStaffName ?: "a staff member"}."
    RemoteEscalationStatus.TRANSFERRED -> "${status.claimedByStaffName ?: "Staff"} has brought in a doctor for further support."
    RemoteEscalationStatus.RESOLVED -> "This request has been marked resolved. We're here if you need to reach out again."
    RemoteEscalationStatus.ABANDONED -> "This request was closed. If you still need support, please use the resources above."
}

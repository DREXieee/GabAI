package com.gabai.presentation.ui.consultation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gabai.domain.repository.AvailableSlot
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAIShapes
import com.gabai.presentation.theme.screenContentPadding
import com.gabai.presentation.util.formatFriendlyDate
import com.gabai.presentation.util.formatFriendlyTime
import androidx.compose.ui.res.painterResource
import com.gabai.R
import com.gabai.presentation.ui.common.GabAILogoButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationBookingScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val viewModel: ConsultationBookingViewModel = viewModel(factory = ConsultationBookingViewModelFactory(context))

    val slots by viewModel.slots.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isBooking by viewModel.isBooking.collectAsState()
    val error by viewModel.error.collectAsState()
    val bookedConsultationId by viewModel.bookedConsultationId.collectAsState()

    var selectedDoctorId by remember { mutableStateOf<String?>(null) }
    var selectedSlot by remember { mutableStateOf<AvailableSlot?>(null) }
    var reasonNote by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadSlots() }

    LaunchedEffect(bookedConsultationId) {
        val id = bookedConsultationId
        if (id != null) {
            navController?.navigate(Routes.consultationDetailRoute(id)) {
                popUpTo(Routes.CONSULTATION_BOOKING) { inclusive = true }
            }
        }
    }

    val title = when {
        selectedSlot != null -> "Confirm Booking"
        selectedDoctorId != null -> "Choose a Time"
        else -> "Choose a Doctor"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    // Mid-flow, the arrow steps back within the booking; at the
                    // first step it's the logo, like every other screen.
                    if (selectedSlot != null || selectedDoctorId != null) {
                        IconButton(onClick = {
                            if (selectedSlot != null) selectedSlot = null else selectedDoctorId = null
                        }) {
                            Icon(painterResource(R.drawable.ic_lucide_arrow_left), contentDescription = "Back")
                        }
                    } else {
                        GabAILogoButton(navController)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues)
        ) {
            when {
                isLoading && slots.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                selectedSlot != null -> ConfirmStep(
                    slot = selectedSlot!!,
                    reasonNote = reasonNote,
                    onReasonNoteChange = { reasonNote = it },
                    isBooking = isBooking,
                    error = error,
                    onConfirm = { viewModel.bookSlot(selectedSlot!!.id, reasonNote) }
                )

                selectedDoctorId != null -> TimeStep(
                    slots = slots.filter { it.doctorId == selectedDoctorId },
                    onSlotSelected = { selectedSlot = it }
                )

                slots.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "No consultation times are available right now. Please check back later.",
                        color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Dimens.SpaceL)
                    )
                }

                else -> DoctorStep(
                    slots = slots,
                    onDoctorSelected = { selectedDoctorId = it }
                )
            }
        }
    }
}

@Composable
private fun DoctorStep(slots: List<AvailableSlot>, onDoctorSelected: (String) -> Unit) {
    val doctors = remember(slots) {
        slots.groupBy { it.doctorId }.map { (id, doctorSlots) -> id to doctorSlots.first().doctorName }
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
        items(doctors, key = { it.first }) { (doctorId, doctorName) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onDoctorSelected(doctorId) },
                shape = GabAIShapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpaceM),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painter = painterResource(R.drawable.ic_lucide_user), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(doctorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TimeStep(slots: List<AvailableSlot>, onSlotSelected: (AvailableSlot) -> Unit) {
    val grouped = remember(slots) { slots.groupBy { formatFriendlyDate(it.startTime) } }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
        grouped.forEach { (dateLabel, dateSlots) ->
            item {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = Dimens.SpaceS, bottom = Dimens.SpaceXXS)
                )
            }
            items(dateSlots, key = { it.id }) { slot ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSlotSelected(slot) },
                    shape = GabAIShapes.medium
                ) {
                    Text(
                        text = "${formatFriendlyTime(slot.startTime)} - ${formatFriendlyTime(slot.endTime)}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(Dimens.SpaceM)
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmStep(
    slot: AvailableSlot,
    reasonNote: String,
    onReasonNoteChange: (String) -> Unit,
    isBooking: Boolean,
    error: String?,
    onConfirm: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
        Card(shape = GabAIShapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(Dimens.SpaceM), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS)) {
                Text(slot.doctorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${formatFriendlyDate(slot.startTime)} • ${formatFriendlyTime(slot.startTime)} - ${formatFriendlyTime(slot.endTime)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        OutlinedTextField(
            value = reasonNote,
            onValueChange = onReasonNoteChange,
            label = { Text("What would you like to talk about? (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = onConfirm,
            enabled = !isBooking,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isBooking) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Confirm Booking")
            }
        }
    }
}

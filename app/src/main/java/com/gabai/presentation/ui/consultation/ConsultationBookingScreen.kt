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
import com.gabai.presentation.util.localDateOf
import com.gabai.presentation.util.rememberReducedMotion
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale

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
        selectedSlot != null -> "Send a Request"
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

// A month calendar of the chosen doctor's open days (days with no slot are
// disabled), with the picked day's times listed underneath.
@Composable
private fun TimeStep(slots: List<AvailableSlot>, onSlotSelected: (AvailableSlot) -> Unit) {
    val slotsByDate = remember(slots) {
        slots.mapNotNull { slot -> localDateOf(slot.startTime)?.let { it to slot } }
            .groupBy({ it.first }, { it.second })
            .toSortedMap()
    }
    if (slotsByDate.isEmpty()) return

    var selectedDate by remember(slotsByDate) { mutableStateOf(slotsByDate.firstKey()) }
    val daysOfWeek = remember { daysOfWeek() }
    val calendarState = rememberCalendarState(
        startMonth = YearMonth.from(slotsByDate.firstKey()),
        endMonth = YearMonth.from(slotsByDate.lastKey()),
        firstVisibleMonth = YearMonth.from(slotsByDate.firstKey()),
        firstDayOfWeek = daysOfWeek.first()
    )
    val dayTimes = slotsByDate[selectedDate].orEmpty()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
        item {
            Column {
                MonthHeader(calendarState)
                Row(modifier = Modifier.fillMaxWidth()) {
                    daysOfWeek.forEach { dayOfWeek ->
                        Text(
                            text = dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.getDefault()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                HorizontalCalendar(
                    state = calendarState,
                    dayContent = { day ->
                        CalendarDayCell(
                            day = day,
                            openTimes = slotsByDate[day.date]?.size ?: 0,
                            isSelected = day.date == selectedDate,
                            onClick = { selectedDate = day.date }
                        )
                    }
                )
            }
        }
        item {
            Text(
                text = "Available times · ${selectedDate.format(SelectedDateFormat)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = Dimens.SpaceS, bottom = Dimens.SpaceXXS)
            )
        }
        items(dayTimes, key = { it.id }) { slot ->
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

private val SelectedDateFormat = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
private val DayDescriptionFormat = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())

@Composable
private fun MonthHeader(calendarState: CalendarState) {
    val reducedMotion = rememberReducedMotion()
    val coroutineScope = rememberCoroutineScope()
    val visibleMonth = calendarState.firstVisibleMonth.yearMonth
    val spansMonths = calendarState.startMonth != calendarState.endMonth

    fun goTo(month: YearMonth) {
        coroutineScope.launch {
            if (reducedMotion) calendarState.scrollToMonth(month) else calendarState.animateScrollToMonth(month)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (spansMonths) {
            IconButton(
                onClick = { goTo(visibleMonth.minusMonths(1)) },
                enabled = visibleMonth > calendarState.startMonth
            ) {
                Icon(painterResource(R.drawable.ic_lucide_chevron_left), contentDescription = "Previous month")
            }
        }
        Text(
            text = visibleMonth.format(MonthTitleFormat),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = Dimens.SpaceXS)
        )
        if (spansMonths) {
            IconButton(
                onClick = { goTo(visibleMonth.plusMonths(1)) },
                enabled = visibleMonth < calendarState.endMonth
            ) {
                Icon(painterResource(R.drawable.ic_lucide_chevron_right), contentDescription = "Next month")
            }
        }
    }
}

private val MonthTitleFormat = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    openTimes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // In/out-dates of the neighbouring months stay blank.
    if (day.position != DayPosition.MonthDate) {
        Box(modifier = Modifier.aspectRatio(1f))
        return
    }
    val isOpen = openTimes > 0
    val description = day.date.format(DayDescriptionFormat) +
        if (isOpen) ", $openTimes open ${if (openTimes == 1) "time" else "times"}" else ", no open times"

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(Dimens.SpaceXXS)
            .clip(CircleShape)
            .then(
                when {
                    isSelected -> Modifier.background(MaterialTheme.colorScheme.primary)
                    isOpen -> Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    else -> Modifier
                }
            )
            .clickable(enabled = isOpen, onClick = onClick)
            .semantics {
                contentDescription = description
                selected = isSelected
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isOpen) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isOpen -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
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

        Text(
            text = "The doctor confirms each request. You can follow it under My Consultations.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                Text("Send Request")
            }
        }
    }
}

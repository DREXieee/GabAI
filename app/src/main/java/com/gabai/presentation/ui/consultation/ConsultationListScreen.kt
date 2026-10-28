package com.gabai.presentation.ui.consultation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.gabai.domain.repository.ConsultationSummary
import com.gabai.domain.repository.RemoteConsultationStatus
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAIShapes
import com.gabai.presentation.theme.screenContentPadding
import com.gabai.presentation.util.formatFriendlyDateTime
import androidx.compose.ui.res.painterResource
import com.gabai.R
import com.gabai.presentation.ui.common.GabAILogoButton
import com.gabai.presentation.ui.common.SkeletonList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationListScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val viewModel: ConsultationListViewModel = viewModel(factory = ConsultationListViewModelFactory(context))

    val consultations by viewModel.consultations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Consultations", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { GabAILogoButton(navController) },
                actions = {
                    if (consultations.isNotEmpty()) {
                        IconButton(onClick = { navController?.navigate(Routes.CONSULTATION_BOOKING) }) {
                            Icon(painterResource(R.drawable.ic_lucide_plus), contentDescription = "Book a consultation")
                        }
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
                isLoading && consultations.isEmpty() -> SkeletonList(
                    loadingDescription = "Loading consultations",
                    rowCount = 4,
                    showLeadingBadge = false
                )

                error != null && consultations.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }

                consultations.isEmpty() -> EmptyState(
                    onBookClick = { navController?.navigate(Routes.CONSULTATION_BOOKING) }
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
                    items(consultations, key = { it.id }) { consultation ->
                        ConsultationCard(
                            consultation = consultation,
                            onClick = { navController?.navigate(Routes.consultationDetailRoute(consultation.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onBookClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lucide_stethoscope),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = Dimens.SpaceM)
        )
        Text(
            text = "No consultations yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Book a live video visit with a mental health professional whenever you're ready.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimens.SpaceXS, bottom = Dimens.SpaceL)
        )
        Button(onClick = onBookClick) {
            Text("Book a Consultation")
        }
    }
}

@Composable
private fun ConsultationCard(consultation: ConsultationSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = GabAIShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS)) {
                Text(
                    text = consultation.doctorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatFriendlyDateTime(consultation.slotStartTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(status = consultation.status)
        }
    }
}

@Composable
private fun StatusBadge(status: RemoteConsultationStatus) {
    val (label, containerColor, contentColor) = when (status) {
        RemoteConsultationStatus.REQUESTED -> Triple(
            "Waiting for confirmation",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurface
        )
        RemoteConsultationStatus.DECLINED -> Triple(
            "Declined",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        RemoteConsultationStatus.CONFIRMED -> Triple(
            "Confirmed",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        RemoteConsultationStatus.CANCELLED -> Triple(
            "Cancelled",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        RemoteConsultationStatus.COMPLETED -> Triple(
            "Completed",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Surface(color = containerColor, shape = RoundedCornerShape(50)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceXXS)
        )
    }
}

package com.gabai.presentation.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gabai.domain.model.HistoryItem
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAIShapes
import com.gabai.presentation.theme.LocalGabAIAccents
import com.gabai.presentation.theme.screenContentPadding
import com.gabai.presentation.util.formatFriendlyDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(context))
    val accents = LocalGabAIAccents.current

    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val error by viewModel.error.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(listState, items.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && items.isNotEmpty() && lastVisibleIndex >= items.size - 3) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                isLoading && items.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                error != null && items.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error) }

                items.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nothing here yet. Your chats, requests, and consultations will show up here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Dimens.SpaceL)
                    )
                }

                else -> LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
                ) {
                    items(items, key = { it.itemKey() }) { historyItem ->
                        HistoryRow(
                            item = historyItem,
                            greenAccent = accents.green,
                            indigoAccent = accents.indigo,
                            onClick = {
                                when (historyItem) {
                                    is HistoryItem.ChatSessionItem ->
                                        navController?.navigate(Routes.chatTranscriptRoute(historyItem.chatSessionId))

                                    is HistoryItem.EscalationItem ->
                                        navController?.navigate(
                                            Routes.escalationRoute(historyItem.crisisLevel, historyItem.escalationId, sessionId = null)
                                        )

                                    is HistoryItem.ConsultationItem ->
                                        navController?.navigate(Routes.consultationDetailRoute(historyItem.consultationId))
                                }
                            }
                        )
                    }
                    if (isLoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(Dimens.SpaceM), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun HistoryItem.itemKey(): String = when (this) {
    is HistoryItem.ChatSessionItem -> "chat-$chatSessionId"
    is HistoryItem.EscalationItem -> "escalation-$escalationId"
    is HistoryItem.ConsultationItem -> "consultation-$consultationId"
}

@Composable
private fun HistoryRow(
    item: HistoryItem,
    greenAccent: Color,
    indigoAccent: Color,
    onClick: () -> Unit
) {
    val (icon, accentColor, title, subtitle) = when (item) {
        is HistoryItem.ChatSessionItem -> HistoryRowContent(
            icon = Icons.Filled.Forum,
            accentColor = greenAccent,
            title = "Chat with GabAI",
            subtitle = buildString {
                append(formatFriendlyDateTime(item.date))
                append(" • ${item.messageCount} messages")
                if (item.hadEscalation) append(" • Escalated")
            }
        )

        is HistoryItem.EscalationItem -> HistoryRowContent(
            icon = Icons.Filled.Warning,
            accentColor = MaterialTheme.colorScheme.error,
            title = "Support Request",
            subtitle = buildString {
                append(formatFriendlyDateTime(item.date))
                append(" • ${item.status.lowercase().replaceFirstChar(Char::uppercase)}")
                if (item.claimedByStaffName != null) append(" • ${item.claimedByStaffName}")
            }
        )

        is HistoryItem.ConsultationItem -> HistoryRowContent(
            icon = Icons.Filled.MedicalServices,
            accentColor = indigoAccent,
            title = "Consultation with ${item.doctorName}",
            subtitle = "${formatFriendlyDateTime(item.slotStartTime)} • " +
                item.status.lowercase().replaceFirstChar(Char::uppercase)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = GabAIShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.IconBadgeSize)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor)
            }
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private data class HistoryRowContent(
    val icon: ImageVector,
    val accentColor: Color,
    val title: String,
    val subtitle: String
)

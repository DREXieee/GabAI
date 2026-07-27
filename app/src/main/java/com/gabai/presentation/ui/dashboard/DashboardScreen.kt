package com.gabai.presentation.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gabai.domain.model.CrisisLevel
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.WindowWidthSize
import com.gabai.presentation.theme.rememberWindowWidthSize
import com.gabai.presentation.theme.screenContentPadding
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    navController: NavController? = null
) {
    val windowWidthSize = rememberWindowWidthSize()

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = if (windowWidthSize == WindowWidthSize.COMPACT) Dp.Unspecified else Dimens.ReadingMaxWidth)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
            ) {
                Spacer(modifier = Modifier.height(Dimens.SpaceM))

                // Welcome Header
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
                ) {
                    Text(
                        text = "Hello!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "How can GabAI help you today?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceM))

                // Quick Action Cards
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val quickActions = remember {
                    listOf(
                        QuickAction(
                            title = "Talk to GabAI",
                            description = "Chat with AI chatbot for mental wellness support",
                            icon = Icons.Default.Email,
                            iconColorRole = QuickActionColorRole.PRIMARY
                        ) { navController?.navigate(Routes.CHATBOT) },
                        QuickAction(
                            title = "Request Consultation",
                            description = "Book an appointment with a mental health professional",
                            icon = Icons.Default.Star,
                            iconColorRole = QuickActionColorRole.TERTIARY
                        ) { navController?.navigate(Routes.CONSULTATION) },
                        QuickAction(
                            title = "Daily Wellness",
                            description = "Track your daily mood and wellness activities",
                            icon = Icons.Default.Favorite,
                            iconColorRole = QuickActionColorRole.SECONDARY
                        ) { /* Navigate to wellness screen (to be implemented) */ },
                        QuickAction(
                            title = "History",
                            description = "View your past sessions and records",
                            icon = Icons.AutoMirrored.Filled.List,
                            iconColorRole = QuickActionColorRole.PRIMARY_CONTAINER
                        ) { /* Navigate to history screen (to be implemented) */ }
                    )
                }

                if (windowWidthSize == WindowWidthSize.EXPANDED) {
                    quickActions.withIndex().toList().chunked(2).forEach { rowActions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                        ) {
                            rowActions.forEach { (index, action) ->
                                ActionCard(
                                    title = action.title,
                                    description = action.description,
                                    icon = action.icon,
                                    iconColor = action.iconColorRole.resolve(),
                                    onClick = action.onClick,
                                    index = index,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    quickActions.forEachIndexed { index, action ->
                        ActionCard(
                            title = action.title,
                            description = action.description,
                            icon = action.icon,
                            iconColor = action.iconColorRole.resolve(),
                            onClick = action.onClick,
                            index = index
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceM))

                // Emergency Support Card
                val emergencyInteractionSource = remember { MutableInteractionSource() }
                val emergencyIsPressed by emergencyInteractionSource.collectIsPressedAsState()
                val emergencyScale by animateFloatAsState(
                    targetValue = if (emergencyIsPressed) 0.97f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                    label = "emergencyCardScale"
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(emergencyScale),
                    onClick = {
                        // Reached from the dashboard, not an active chat -- no session/escalation
                        // to reference yet, so EscalationScreen falls back to a fresh manual request.
                        navController?.navigate(Routes.escalationRoute(CrisisLevel.NONE, escalationId = null, sessionId = null))
                    },
                    interactionSource = emergencyInteractionSource,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.SpaceM),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Emergency",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(Dimens.IconSizeL)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS)
                        ) {
                            Text(
                                text = "Emergency Support",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "If you are in an emergency, call 911 immediately",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceM))
            }
        }
    }
}

private data class QuickAction(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColorRole: QuickActionColorRole,
    val onClick: () -> Unit
)

private enum class QuickActionColorRole {
    PRIMARY, SECONDARY, TERTIARY, PRIMARY_CONTAINER;

    @Composable
    fun resolve(): Color = when (this) {
        PRIMARY -> MaterialTheme.colorScheme.primary
        SECONDARY -> MaterialTheme.colorScheme.secondary
        TERTIARY -> MaterialTheme.colorScheme.tertiary
        PRIMARY_CONTAINER -> MaterialTheme.colorScheme.primaryContainer
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    val visibleState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        visibleState.targetState = true
    }

    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = fadeIn(tween(400, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(400, easing = FastOutSlowInEasing)) { it / 4 }
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
            label = "actionCardScale"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.ActionCardHeight)
                .scale(scale),
            onClick = onClick,
            interactionSource = interactionSource,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.SpaceM),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically
            ) {
            Card(
                modifier = Modifier.size(Dimens.IconBadgeSize),
                colors = CardDefaults.cardColors(
                    containerColor = iconColor.copy(alpha = 0.1f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(Dimens.IconSizeM)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            }
        }
    }
}

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
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.gabai.presentation.theme.GabAIShapes
import com.gabai.presentation.theme.LocalGabAIAccents
import com.gabai.presentation.theme.WindowWidthSize
import com.gabai.presentation.theme.rememberWindowWidthSize
import com.gabai.presentation.theme.screenContentPadding
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    navController: NavController? = null
) {
    val windowWidthSize = rememberWindowWidthSize()
    val accents = LocalGabAIAccents.current

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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "How can GabAI help you today?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceM))

                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val quickActions = remember(accents) {
                    listOf(
                        QuickAction(
                            title = "Talk to GabAI",
                            description = "Chat for mental wellness support",
                            icon = Icons.Filled.Forum,
                            color = accents.green
                        ) { navController?.navigate(Routes.CHATBOT) },
                        QuickAction(
                            title = "Request Consultation",
                            description = "Book with a mental health professional",
                            icon = Icons.Filled.MedicalServices,
                            color = accents.indigo
                        ) { navController?.navigate(Routes.CONSULTATION) },
                        QuickAction(
                            title = "Daily Wellness",
                            description = "Track your mood and wellness",
                            icon = Icons.Filled.Spa,
                            color = accents.pink
                        ) { navController?.navigate(Routes.WELLNESS) },
                        QuickAction(
                            title = "History",
                            description = "View your past sessions",
                            icon = Icons.Filled.History,
                            color = accents.amber
                        ) { navController?.navigate(Routes.HISTORY) }
                    )
                }

                val columns = 2

                quickActions.withIndex().toList().chunked(columns).forEach { rowActions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                    ) {
                        rowActions.forEach { (index, action) ->
                            ActionCard(
                                title = action.title,
                                description = action.description,
                                icon = action.icon,
                                accentColor = action.color,
                                onClick = action.onClick,
                                index = index,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowActions.size < columns) {
                            Spacer(modifier = Modifier.weight((columns - rowActions.size).toFloat()))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceM))

                // Emergency Support Card -- deliberately stays on the dedicated crisis-red
                // errorContainer, never folded into the decorative accent rotation above.
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
                    shape = GabAIShapes.large,
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
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
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

        // Bold color-blocked card (accent as the full background, white text/icon)
        // -- this is the "richer iconography instead of illustration" treatment:
        // a large white icon badge stands in for the reference mockup's character art.
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.ActionCardHeight + Dimens.SpaceL)
                .scale(scale),
            onClick = onClick,
            interactionSource = interactionSource,
            shape = GabAIShapes.large,
            colors = CardDefaults.cardColors(containerColor = accentColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.SpaceM),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.IconBadgeSize)
                        .background(Color.White.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(Dimens.IconSizeM)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

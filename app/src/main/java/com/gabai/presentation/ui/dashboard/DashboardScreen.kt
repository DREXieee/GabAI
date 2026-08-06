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
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gabai.R
import com.gabai.data.local.repository.AuthSessionStore
import com.gabai.domain.model.CrisisLevel
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAIShapes
import com.gabai.presentation.theme.LocalGabAIAccents
import com.gabai.presentation.theme.WindowWidthSize
import com.gabai.presentation.theme.rememberWindowWidthSize
import com.gabai.presentation.theme.screenContentPadding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController? = null
) {
    val windowWidthSize = rememberWindowWidthSize()
    val accents = LocalGabAIAccents.current
    val context = LocalContext.current

    var displayName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        displayName = AuthSessionStore(context).session.first()?.displayName.orEmpty()
    }
    val firstName = displayName.trim().substringBefore(" ").ifBlank { null }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.logo_gabai),
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.IconBadgeSize - Dimens.SpaceM)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceXS))
                        Text(
                            text = "GabAI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController?.navigate(Routes.SETTINGS) }) {
                        if (firstName != null) {
                            Box(
                                modifier = Modifier
                                    .size(Dimens.IconBadgeSize - Dimens.SpaceM)
                                    .background(accents.indigo, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = firstName.first().uppercaseChar().toString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // Emergency Support is a sibling below the scrollable region, not part of
        // its content -- that's what guarantees it's always visible without
        // scrolling, regardless of grid height or device size, rather than trying
        // to shrink the grid to fit an arbitrary viewport (fragile across Android's
        // huge range of screen heights and accessibility font-scaling settings).
        val readingMaxWidth = if (windowWidthSize == WindowWidthSize.COMPACT) Dp.Unspecified else Dimens.ReadingMaxWidth

        Column(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    // widthIn's max must be applied before fillMaxWidth -- fillMaxWidth
                    // forces an exact (min==max) width constraint, and a widthIn placed
                    // after it can no longer shrink that exact constraint back down, so
                    // the reading-width cap silently did nothing on wide/tablet screens
                    // until this order was fixed (verified live on-device: content filled
                    // the full ~922dp width instead of capping at 640dp).
                    modifier = Modifier
                        .widthIn(max = readingMaxWidth)
                        .fillMaxWidth()
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
                            text = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (firstName != null) "${timeOfDayGreeting()}, $firstName!" else "Hello!",
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

                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Interlocking bento grid: left column runs tall-then-small, right
                    // column runs small-then-tall, so the two tall tiles sit diagonally
                    // from each other -- this is what makes it read as "interlocking"
                    // rather than just a hero tile beside a stack. Both columns sum to
                    // the same total height (2 units + 1 unit + the gap between them).
                    val unit = Dimens.ActionCardHeight + Dimens.SpaceL
                    val tallUnit = unit * 2 + Dimens.SpaceM

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                        ) {
                            HeroActionCard(
                                title = "Talk to GabAI",
                                description = "Chat for mental wellness support, any time",
                                icon = Icons.Filled.Forum,
                                accentColor = accents.green,
                                onClick = { navController?.navigate(Routes.CHATBOT) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tallUnit),
                                index = 0
                            )
                            ActionCard(
                                title = "History",
                                description = "View your past sessions",
                                icon = Icons.Filled.History,
                                accentColor = accents.amber,
                                onClick = { navController?.navigate(Routes.HISTORY) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(unit),
                                index = 3
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                        ) {
                            ActionCard(
                                title = "Request Consultation",
                                description = "Book with a professional",
                                icon = Icons.Filled.MedicalServices,
                                accentColor = accents.indigo,
                                onClick = { navController?.navigate(Routes.CONSULTATION) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(unit),
                                index = 1
                            )
                            HeroActionCard(
                                title = "Daily Wellness",
                                description = "Track your mood and wellness",
                                icon = Icons.Filled.Spa,
                                accentColor = accents.pink,
                                onClick = { navController?.navigate(Routes.WELLNESS) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tallUnit),
                                index = 2
                            )
                        }
                    }

                    // Full-width footer tile -- mirrors the inspiration's wide "Nature
                    // Vibe" card, reusing the hero green accent the way the reference
                    // reuses its own green across the tall tile and the footer.
                    WideActionCard(
                        title = "Nearest Clinics",
                        description = "Find psychological clinics near you",
                        icon = Icons.Filled.LocationOn,
                        accentColor = accents.green,
                        onClick = { navController?.navigate(Routes.NEAREST_CLINICS) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(unit),
                        index = 4
                    )

                    Spacer(modifier = Modifier.height(Dimens.SpaceM))
                }
            }

            // Emergency Support -- pinned below the scrollable region (not inside
            // it) so it's always visible without scrolling. Shares the grid's width
            // cap/centering and shape/padding for visual consistency with the rest
            // of the screen.
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                EmergencySupportBanner(
                    onClick = {
                        // Reached from the dashboard, not an active chat -- no session/escalation
                        // to reference yet, so EscalationScreen falls back to a fresh manual request.
                        navController?.navigate(Routes.escalationRoute(CrisisLevel.NONE, escalationId = null, sessionId = null))
                    },
                    modifier = Modifier
                        .widthIn(max = readingMaxWidth)
                        .fillMaxWidth()
                        .padding(top = Dimens.SpaceM, bottom = Dimens.SpaceM)
                )
            }
        }
    }
}

// Deliberately stays on the dedicated crisis-red errorContainer, never folded
// into the decorative accent rotation used by the bento grid cards.
@Composable
private fun EmergencySupportBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "emergencyCardScale"
    )
    Card(
        modifier = modifier.scale(scale),
        onClick = onClick,
        interactionSource = interactionSource,
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
}

private fun timeOfDayGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}

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
        // Sized entirely by the caller's modifier (bento tiles vary in size) rather
        // than a fixed height here.
        Card(
            modifier = Modifier
                .fillMaxSize()
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

// The bento grid's hero tile: same color-blocked treatment as ActionCard but
// with a larger icon badge and headline-weight title, since it spans double
// the height of the tiles beside it.
@Composable
private fun HeroActionCard(
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
            label = "heroActionCardScale"
        )

        Card(
            modifier = Modifier
                .fillMaxSize()
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
                        .size(Dimens.IconBadgeLargeSize)
                        .background(Color.White.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(Dimens.IconSizeL)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 3
                    )
                }
            }
        }
    }
}

// The bento grid's full-width footer tile: same color-blocked treatment, laid
// out horizontally (icon beside text) rather than stacked, since it spans the
// full row width instead of a narrow column.
@Composable
private fun WideActionCard(
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
            label = "wideActionCardScale"
        )

        Card(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale),
            onClick = onClick,
            interactionSource = interactionSource,
            shape = GabAIShapes.large,
            colors = CardDefaults.cardColors(containerColor = accentColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.SpaceM),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically
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

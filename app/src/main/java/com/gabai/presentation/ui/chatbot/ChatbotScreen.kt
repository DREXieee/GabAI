package com.gabai.presentation.ui.chatbot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gabai.data.remote.api.BackendChatRepositoryImpl
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.domain.model.ChatMessage
import com.gabai.domain.model.CrisisLevel
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.PillShape
import com.gabai.presentation.theme.WindowWidthSize
import com.gabai.presentation.theme.rememberWindowWidthSize
import com.gabai.presentation.theme.screenContentPadding
import com.gabai.presentation.ui.common.SafetyDisclaimerLine
import com.gabai.presentation.ui.common.SafetyResourcesCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    navController: NavController? = null,
    viewModel: ChatViewModel = run {
        val context = LocalContext.current
        viewModel(
            factory = ChatViewModelFactory(
                BackendChatRepositoryImpl(RetrofitClient.getApiService(context))
            )
        )
    }
) {
    var messageText by remember { mutableStateOf("") }
    val messages = viewModel.messages
    val crisisLevel by viewModel.crisisLevel.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val escalationId by viewModel.escalationId.collectAsState()
    val sessionId by viewModel.sessionId.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GabAI Assistant",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues)
        ) {
            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = Dimens.SpaceM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatBubble(
                        message = message,
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(300),
                            placementSpec = tween(300)
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = isTyping,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150))
            ) {
                TypingIndicator(modifier = Modifier.padding(horizontal = Dimens.SpaceXS))
            }

            // Emergency Warning - only escalates for the rest of this chat session,
            // never silently cleared by a later, calmer message.
            AnimatedVisibility(
                visible = crisisLevel != CrisisLevel.NONE,
                enter = fadeIn(tween(400, easing = FastOutSlowInEasing)) +
                    expandVertically(tween(400, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(250)) + shrinkVertically(tween(250))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.SpaceXS),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimens.SpaceM),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
                        ) {
                            Text(
                                text = "⚠️ Emergency Support",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = if (crisisLevel == CrisisLevel.SEVERE)
                                    "It sounds like you're going through a very difficult time. Please consider speaking with a mental health professional."
                                else
                                    "It sounds like things have been really hard lately. Would you like some support resources?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Button(
                                onClick = {
                                    navController?.navigate(
                                        Routes.escalationRoute(crisisLevel, escalationId, sessionId)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Speak to a Professional")
                            }
                        }
                    }
                    // Full hotline card reserved for SEVERE; MODERATE stays softer/less alarming.
                    if (crisisLevel == CrisisLevel.SEVERE) {
                        SafetyResourcesCard()
                    }
                }
            }

            // Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpaceXS),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type your message...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    shape = PillShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    modifier = Modifier
                        .size(Dimens.SendButtonSize)
                        .background(
                            color = if (messageText.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = PillShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (messageText.isNotBlank())
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SafetyDisclaimerLine(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpaceXXS)
            )
        }
    }
}

// Not private -- reused read-only by ChatTranscriptScreen for History's chat tap-through.
@Composable
fun ChatBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    val windowWidthSize = rememberWindowWidthSize()
    val maxBubbleWidth = if (windowWidthSize == WindowWidthSize.COMPACT)
        Dimens.ChatBubbleMaxWidthCompact
    else
        Dimens.ChatBubbleMaxWidthWide

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxBubbleWidth)
                .background(
                    color = if (message.isUser)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = if (message.isUser) Dimens.SpaceM else Dimens.SpaceXXS,
                        topEnd = if (message.isUser) Dimens.SpaceXXS else Dimens.SpaceM,
                        bottomStart = Dimens.SpaceM,
                        bottomEnd = Dimens.SpaceM
                    )
                )
                .padding(Dimens.SpaceS)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TypingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typingIndicator")
    Row(
        modifier = modifier.padding(vertical = Dimens.SpaceXXS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS)
    ) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 150),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "typingDot$index"
            )
            Box(
                modifier = Modifier
                    .size(Dimens.TypingDotSize)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                        shape = CircleShape
                    )
            )
        }
    }
}

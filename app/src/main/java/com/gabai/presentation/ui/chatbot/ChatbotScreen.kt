package com.gabai.presentation.ui.chatbot

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gabai.R
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
import kotlinx.coroutines.delay
import java.io.File
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.activity.compose.BackHandler

// Auto-stop tuning for the mic button's silence detection -- a panic-attack
// user shouldn't have to remember to tap stop. getMaxAmplitude()'s scale
// varies by device/mic gain, so this threshold may need real-device tuning;
// SPEECH_GRACE skips the initial pre-speech beat so it isn't mistaken for
// "already done talking."
private const val SILENCE_AMPLITUDE_THRESHOLD = 2000
private const val SILENCE_DURATION_MS = 1500L
private const val SPEECH_GRACE_MS = 800L
private const val MAX_RECORDING_MS = 60_000L
private const val AMPLITUDE_POLL_INTERVAL_MS = 150L

private enum class SpeechPlaybackState { IDLE, PLAYING, PAUSED }

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
    val isTranscribing by viewModel.isTranscribing.collectAsState()
    val voiceError by viewModel.voiceError.collectAsState()
    val listState = rememberLazyListState()

    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    fun startRecording() {
        val file = File.createTempFile("gabai-voice", ".m4a", context.cacheDir)
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        try {
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            audioFile = file
            isRecording = true
        } catch (e: Exception) {
            recorder.release()
        }
    }

    fun stopRecordingAndSend() {
        val recorder = mediaRecorder ?: return
        val file = audioFile
        try {
            recorder.stop()
        } catch (e: Exception) {
            // A stop() this soon after start() (e.g. an accidental tap) throws --
            // there's no usable audio either way, so just drop it.
        }
        recorder.release()
        mediaRecorder = null
        isRecording = false
        if (file != null && file.length() > 0) {
            viewModel.sendVoiceMessage(file.readBytes(), "audio/m4a")
            file.delete()
        }
    }

    // Auto-stop once the user has gone quiet for a bit, so they don't have
    // to remember to tap stop themselves. Tap-to-stop (above) still works as
    // an override to cut a recording short.
    LaunchedEffect(isRecording) {
        if (!isRecording) return@LaunchedEffect
        val startedAt = System.currentTimeMillis()
        var silenceStartedAt: Long? = null
        while (isRecording) {
            delay(AMPLITUDE_POLL_INTERVAL_MS)
            val elapsed = System.currentTimeMillis() - startedAt
            if (elapsed >= MAX_RECORDING_MS) {
                stopRecordingAndSend()
                break
            }
            if (elapsed < SPEECH_GRACE_MS) continue

            val amplitude = try {
                mediaRecorder?.maxAmplitude ?: break
            } catch (e: IllegalStateException) {
                break
            }
            if (amplitude < SILENCE_AMPLITUDE_THRESHOLD) {
                val silenceStart = silenceStartedAt ?: System.currentTimeMillis().also { silenceStartedAt = it }
                if (System.currentTimeMillis() - silenceStart >= SILENCE_DURATION_MS) {
                    stopRecordingAndSend()
                    break
                }
            } else {
                silenceStartedAt = null
            }
        }
    }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) startRecording() }

    var speechPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var speechPlaybackState by remember { mutableStateOf(SpeechPlaybackState.IDLE) }

    fun toggleSpeechPlayback() {
        val player = speechPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            speechPlaybackState = SpeechPlaybackState.PAUSED
        } else {
            player.start()
            speechPlaybackState = SpeechPlaybackState.PLAYING
        }
    }

    // Plays each assistant reply's synthesized speech as it arrives -- a
    // SharedFlow (not StateFlow) so a config change doesn't replay the last clip.
    LaunchedEffect(Unit) {
        viewModel.speechEvents.collect { audioBytes ->
            try {
                // Only one reply plays at a time -- a new one interrupts whatever's still going.
                speechPlayer?.release()
                val file = File.createTempFile("gabai-tts", ".mp3", context.cacheDir)
                file.writeBytes(audioBytes)
                val player = MediaPlayer()
                player.setDataSource(file.absolutePath)
                player.setOnCompletionListener {
                    it.release()
                    file.delete()
                    speechPlayer = null
                    speechPlaybackState = SpeechPlaybackState.IDLE
                }
                player.prepare()
                player.start()
                speechPlayer = player
                speechPlaybackState = SpeechPlaybackState.PLAYING
            } catch (e: Exception) {
                // Silent -- read-aloud is a best-effort convenience layer.
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { speechPlayer?.release() }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Chat is the root screen, so the system back button opens the menu
    // (like tapping the logo) instead of exiting; the dashboard's own back
    // handler is what leaves the app.
    BackHandler {
        navController?.navigate(Routes.DASHBOARD) { launchSingleTop = true }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Same logo + name as the dashboard's top bar; tapping it
                    // opens that dashboard (the menu). Chat is the root screen,
                    // so there is deliberately no back arrow here.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(Dimens.SpaceXS))
                            .clickable(
                                onClickLabel = "Open menu",
                                role = Role.Button,
                                onClick = {
                                    navController?.navigate(Routes.DASHBOARD) { launchSingleTop = true }
                                }
                            )
                            .padding(vertical = Dimens.SpaceXXS, horizontal = Dimens.SpaceXXS)
                    ) {
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
                // Without this, the keyboard just overlays the screen instead of
                // resizing it (this app draws edge-to-edge, so the system no
                // longer does that automatically) -- the messages list below,
                // being the only weight(1f) child, would end up hidden behind
                // the keyboard instead of shrinking to stay visible above it.
                .imePadding()
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

            if (isTranscribing) {
                Text(
                    text = "Listening…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Dimens.SpaceXXS)
                )
            }
            voiceError?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = Dimens.SpaceXXS)
                )
            }

            if (speechPlaybackState != SpeechPlaybackState.IDLE) {
                TextButton(
                    onClick = { toggleSpeechPlayback() },
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceXXS)
                ) {
                    Icon(
                        painter = painterResource(
                            if (speechPlaybackState == SpeechPlaybackState.PLAYING)
                            R.drawable.ic_lucide_pause
                        else
                            R.drawable.ic_lucide_play
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.SpaceM)
                    )
                    Text(
                        text = if (speechPlaybackState == SpeechPlaybackState.PLAYING)
                            "Pause voice reply"
                        else
                            "Resume voice reply",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = Dimens.SpaceXXS)
                    )
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
                    enabled = !isRecording && !isTranscribing,
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
                    enabled = messageText.isNotBlank() && !isRecording && !isTranscribing,
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
                        painter = painterResource(R.drawable.ic_lucide_send_horizontal),
                        contentDescription = "Send",
                        tint = if (messageText.isNotBlank())
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tap-to-toggle rather than press-and-hold -- more reliable across
                // devices than a held gesture, and doesn't require sustained
                // contact with the screen through a panic response.
                IconButton(
                    onClick = {
                        if (isRecording) {
                            stopRecordingAndSend()
                        } else {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                startRecording()
                            } else {
                                recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    enabled = !isTranscribing,
                    modifier = Modifier
                        .size(Dimens.SendButtonSize)
                        .background(
                            color = if (isRecording)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = PillShape
                        )
                ) {
                    Icon(
                        painter = painterResource(if (isRecording) R.drawable.ic_lucide_square else R.drawable.ic_lucide_mic),
                        contentDescription = if (isRecording) "Stop recording" else "Record a voice message",
                        tint = if (isRecording)
                            MaterialTheme.colorScheme.onError
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

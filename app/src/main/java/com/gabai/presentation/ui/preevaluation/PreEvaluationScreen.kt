package com.gabai.presentation.ui.preevaluation

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gabai.R
import com.gabai.data.local.repository.AuthSessionStore
import com.gabai.domain.model.CrisisLevel
import com.gabai.domain.model.PreEvaluationQuestions
import com.gabai.domain.model.PreEvaluationResult
import com.gabai.domain.model.severityLabel
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAITheme
import com.gabai.presentation.theme.Gold
import com.gabai.presentation.theme.Ink
import com.gabai.presentation.theme.PillShape
import com.gabai.presentation.theme.screenContentPadding
import com.gabai.presentation.ui.common.SafetyResourcesCard
import kotlinx.coroutines.launch

// The PHQ-9 + GAD-7 pre-evaluation every patient answers once, after sign-up
// and before the chat (see NavGraph). It has no menu button, since the menu
// would skip it, but help is always one tap away: "Get help" shows the hotlines.
@Composable
fun PreEvaluationScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val viewModel: PreEvaluationViewModel = viewModel(factory = PreEvaluationViewModelFactory(context))
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val authStore = remember { AuthSessionStore(context) }

    LaunchedEffect(state.alreadyDone) {
        if (state.alreadyDone) navController?.leavePreEvaluation(then = null)
    }

    BackHandler(enabled = state.step > 0 && state.result == null && !state.isSubmitting) { viewModel.back() }

    PreEvaluationContent(
        state = state,
        onStart = viewModel::start,
        onAnswer = viewModel::answer,
        onNext = viewModel::next,
        onBack = viewModel::back,
        // GabAINavGraph sends a cleared session back to the sign-in screen.
        onLogOut = { coroutineScope.launch { authStore.clearSession() } },
        onContinue = { navController?.leavePreEvaluation(then = null) },
        onBookConsultation = { navController?.leavePreEvaluation(then = Routes.CONSULTATION_BOOKING) },
        onWaitForTeam = { result ->
            navController?.leavePreEvaluation(
                then = Routes.escalationRoute(CrisisLevel.SEVERE, result.escalationId, result.escalationSessionId)
            )
        }
    )
}

// Swaps the pre-evaluation for the chat (the app's home), then optionally opens
// another screen on top, so Back from there lands on the chat, never the questions.
private fun NavController.leavePreEvaluation(then: String?) {
    navigate(Routes.CHATBOT) {
        popUpTo(Routes.PRE_EVALUATION) { inclusive = true }
    }
    then?.let { navigate(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreEvaluationContent(
    state: PreEvaluationUiState,
    modifier: Modifier = Modifier,
    onStart: () -> Unit = {},
    onAnswer: (Int) -> Unit = {},
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    onLogOut: () -> Unit = {},
    onContinue: () -> Unit = {},
    onBookConsultation: () -> Unit = {},
    onWaitForTeam: (PreEvaluationResult) -> Unit = {}
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    val result = state.result
    val isAnswering = !state.isChecking && result == null && state.step > 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Pre-evaluation", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                actions = {
                    if (result == null) {
                        TextButton(onClick = { showHelp = !showHelp }, colors = onSurfaceTextButtonColors()) {
                            Text(if (showHelp) "Hide help" else "Get help")
                        }
                        IconButton(onClick = onLogOut) {
                            Icon(painterResource(R.drawable.ic_lucide_log_out), contentDescription = "Log out")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isAnswering) {
                QuestionNavigation(
                    isFirstQuestion = state.step == 1,
                    isLastQuestion = state.step == state.questionCount,
                    canContinue = state.answers[state.questionIndex] != null && !state.isSubmitting,
                    isSubmitting = state.isSubmitting,
                    onBack = onBack,
                    onNext = onNext
                )
            }
        }
    ) { paddingValues ->
        if (state.isChecking) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = Dimens.ReadingMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
            ) {
                Spacer(Modifier.height(Dimens.SpaceXS))

                if (showHelp && result == null) SafetyResourcesCard()

                when {
                    result != null -> ResultContent(result, onContinue, onBookConsultation, onWaitForTeam)
                    state.step == 0 -> IntroContent(onStart)
                    else -> QuestionContent(
                        index = state.questionIndex,
                        count = state.questionCount,
                        selected = state.answers[state.questionIndex],
                        error = state.error,
                        onAnswer = onAnswer
                    )
                }

                Spacer(Modifier.height(Dimens.SpaceM))
            }
        }
    }
}

@Composable
private fun IntroContent(onStart: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
        Box(
            modifier = Modifier
                .size(Dimens.AuthIconBadgeSize)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_lucide_heart),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(Dimens.IconSizeL)
            )
        }

        Text(
            text = "Before we start",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "A few questions about how you've been feeling over the last 2 weeks. They come from " +
                "two short check-ins that doctors use, the PHQ-9 and the GAD-7, and take about 3 minutes.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "There are no right or wrong answers. This is a screening, not a diagnosis. Your answers " +
                "help GabAI support you, and the GabAI staff and doctors who help you can see them.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        PrimaryButton(text = "Start", onClick = onStart)
    }
}

@Composable
private fun QuestionContent(
    index: Int,
    count: Int,
    selected: Int?,
    error: String?,
    onAnswer: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)) {
            Text(
                text = "Question ${index + 1} of $count",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            LinearProgressIndicator(
                progress = { (index + 1) / count.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Text(
            text = PreEvaluationQuestions.PROMPT,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = PreEvaluationQuestions.ALL[index],
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (index == PreEvaluationQuestions.PHQ9.lastIndex) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "If you're in danger right now, call 911 or tap Get help for hotlines.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(Dimens.SpaceS)
                )
            }
        }

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
        ) {
            PreEvaluationQuestions.ANSWER_LABELS.forEachIndexed { value, label ->
                AnswerOption(label = label, selected = selected == value, onClick = { onAnswer(value) })
            }
        }

        if (error != null) {
            Text(text = error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun AnswerOption(label: String, selected: Boolean, onClick: () -> Unit) {
    // Selected = filled, a thicker accent border and the radio dot. The text and
    // dot stay onSurface: coral text or dots would fall below contrast minimums.
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = Dimens.ButtonHeight)
                .padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceXS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
        ) {
            // The whole row is the radio button; this one is only its visual.
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.onSurface,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun QuestionNavigation(
    isFirstQuestion: Boolean,
    isLastQuestion: Boolean,
    canContinue: Boolean,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                enabled = !isSubmitting,
                modifier = Modifier.heightIn(min = Dimens.ButtonHeight),
                colors = onSurfaceTextButtonColors()
            ) {
                Text(if (isFirstQuestion) "Back to intro" else "Back")
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onNext,
                enabled = canContinue,
                shape = PillShape,
                modifier = Modifier.heightIn(min = Dimens.ButtonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Ink,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(Dimens.SpaceM), color = Ink, strokeWidth = 2.dp)
                } else {
                    Text(if (isLastQuestion) "See my results" else "Next", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ResultContent(
    result: PreEvaluationResult,
    onContinue: () -> Unit,
    onBookConsultation: () -> Unit,
    onWaitForTeam: (PreEvaluationResult) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
        if (result.selfHarm) {
            Text(
                text = "Thank you for telling us",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "You said you've had thoughts of hurting yourself or of being better off dead. You " +
                    "don't have to carry that alone. We've let the GabAI care team know, and a team member " +
                    "can call you right here in the app.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = { onWaitForTeam(result) },
                modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.ButtonHeight),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(painterResource(R.drawable.ic_lucide_phone), contentDescription = null)
                Spacer(Modifier.size(Dimens.SpaceXS))
                Text("Wait for a GabAI team member", fontWeight = FontWeight.SemiBold)
            }
            SafetyResourcesCard()
        } else {
            Text(
                text = "Thank you for sharing",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Dimens.SpaceM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
            ) {
                ScoreRow(
                    title = "Low mood (PHQ-9)",
                    severity = result.phq9Severity,
                    score = result.phq9Score,
                    maxScore = PreEvaluationQuestions.PHQ9_MAX_SCORE
                )
                ScoreRow(
                    title = "Worry and anxiety (GAD-7)",
                    severity = result.gad7Severity,
                    score = result.gad7Score,
                    maxScore = PreEvaluationQuestions.GAD7_MAX_SCORE
                )
            }
        }

        Text(
            text = resultSummary(result),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "This is a screening, not a diagnosis. Only a doctor or psychologist can diagnose a condition.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (result.selfHarm) {
            OutlinedButton(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.ButtonHeight),
                shape = PillShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("Continue to GabAI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        } else {
            PrimaryButton(text = "Continue to GabAI", onClick = onContinue)
            if (result.suggestsDoctor) {
                OutlinedButton(
                    onClick = onBookConsultation,
                    modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.ButtonHeight),
                    shape = PillShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Book a consultation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun resultSummary(result: PreEvaluationResult): String = when {
    result.suggestsDoctor ->
        "Your answers suggest you've been carrying a lot lately. Talking with a doctor could help, and you " +
            "can book a consultation whenever you're ready. GabAI is here to talk in the meantime."
    result.phq9Score >= 5 || result.gad7Score >= 5 ->
        "Your answers suggest some stress lately. GabAI is here whenever you want to talk it through."
    else -> "Your answers suggest you've been doing okay lately. GabAI is here whenever you need someone to talk to."
}

@Composable
private fun ScoreRow(title: String, severity: String, score: Int, maxScore: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${severityLabel(severity)} · $score of $maxScore",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// The theme's coral primary is too light for text on white, so text buttons here
// use onSurface, as does secondary text: onSurfaceVariant is ~3.3:1 on white.
@Composable
private fun onSurfaceTextButtonColors() = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = PillShape,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.ButtonHeight),
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

private val previewQuestionState = PreEvaluationUiState(
    isChecking = false,
    step = 2,
    answers = List(PreEvaluationQuestions.ALL.size) { if (it == 1) 2 else null }
)

private val previewResult = PreEvaluationResult(
    phq9Score = 12,
    phq9Severity = "MODERATE",
    gad7Score = 7,
    gad7Severity = "MILD",
    selfHarm = false,
    escalationId = null,
    escalationSessionId = null
)

@Preview(name = "Question, light", showBackground = true)
@Composable
private fun PreEvaluationQuestionPreview() {
    GabAITheme(darkTheme = false) { PreEvaluationContent(state = previewQuestionState) }
}

@Preview(name = "Question, dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreEvaluationQuestionDarkPreview() {
    GabAITheme(darkTheme = true) { PreEvaluationContent(state = previewQuestionState) }
}

@Preview(name = "Result, light", showBackground = true)
@Composable
private fun PreEvaluationResultPreview() {
    GabAITheme(darkTheme = false) {
        PreEvaluationContent(state = PreEvaluationUiState(isChecking = false, result = previewResult))
    }
}

@Preview(name = "Result, dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreEvaluationResultDarkPreview() {
    GabAITheme(darkTheme = true) {
        PreEvaluationContent(state = PreEvaluationUiState(isChecking = false, result = previewResult))
    }
}

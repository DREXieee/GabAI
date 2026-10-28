package com.gabai.presentation.navigation

import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gabai.data.local.repository.AuthSessionStore
import com.gabai.domain.model.CrisisLevel
import com.gabai.presentation.ui.auth.CompleteProfileScreen
import com.gabai.presentation.ui.auth.ForgotPasswordScreen
import com.gabai.presentation.ui.auth.LoginScreen
import com.gabai.presentation.ui.auth.RegistrationScreen
import kotlinx.coroutines.flow.first
import com.gabai.presentation.ui.call.CallScreen
import com.gabai.presentation.ui.chatbot.ChatbotScreen
import com.gabai.presentation.ui.clinics.NearestClinicsScreen
import com.gabai.presentation.ui.consultation.ConsultationBookingScreen
import com.gabai.presentation.ui.consultation.ConsultationDetailScreen
import com.gabai.presentation.ui.consultation.ConsultationListScreen
import com.gabai.presentation.ui.dashboard.DashboardScreen
import com.gabai.presentation.ui.escalation.EscalationScreen
import com.gabai.presentation.ui.history.ChatTranscriptScreen
import com.gabai.presentation.ui.history.HistoryScreen
import com.gabai.presentation.ui.settings.SettingsScreen
import com.gabai.presentation.ui.wellness.WellnessDetailScreen
import com.gabai.presentation.ui.wellness.WellnessScreen

private const val ARG_CRISIS_LEVEL = "crisisLevel"
private const val ARG_ESCALATION_ID = "escalationId"
private const val ARG_SESSION_ID = "sessionId"
private const val ARG_ROOM_URL = "roomUrl"
private const val ARG_TOKEN = "token"
private const val ARG_WELLNESS_ITEM_ID = "itemId"
private const val ARG_CONSULTATION_ID = "consultationId"

// Navigation routes. The chat is the app's home: login, registration and a
// restored session all land on CHATBOT, and the logo in its top bar opens
// DASHBOARD (the menu) on top of it.
object Routes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val COMPLETE_PROFILE = "complete_profile"
    const val FORGOT_PASSWORD = "forgot_password"
    const val DASHBOARD = "dashboard"
    const val CHATBOT = "chatbot"
    const val ESCALATION = "escalation"
    const val CALL = "call"
    const val CONSULTATION = "consultation"
    const val SETTINGS = "settings"
    const val WELLNESS = "wellness"
    const val WELLNESS_DETAIL = "wellness_detail"
    const val CONSULTATION_BOOKING = "consultation_booking"
    const val CONSULTATION_DETAIL = "consultation_detail"
    const val HISTORY = "history"
    const val CHAT_TRANSCRIPT = "chat_transcript"
    const val NEAREST_CLINICS = "nearest_clinics"

    // escalationId/sessionId are server-issued, so they must be optional query
    // params, not path segments -- LOCAL_KEYWORD/FALLBACK responses that never
    // reached the backend have neither (see ChatViewModel).
    const val ESCALATION_ROUTE =
        "$ESCALATION/{$ARG_CRISIS_LEVEL}?$ARG_ESCALATION_ID={$ARG_ESCALATION_ID}&$ARG_SESSION_ID={$ARG_SESSION_ID}"

    const val CALL_ROUTE = "$CALL?$ARG_ROOM_URL={$ARG_ROOM_URL}&$ARG_TOKEN={$ARG_TOKEN}"

    const val WELLNESS_DETAIL_ROUTE = "$WELLNESS_DETAIL/{$ARG_WELLNESS_ITEM_ID}"
    const val CONSULTATION_DETAIL_ROUTE = "$CONSULTATION_DETAIL/{$ARG_CONSULTATION_ID}"
    const val CHAT_TRANSCRIPT_ROUTE = "$CHAT_TRANSCRIPT/{$ARG_SESSION_ID}"

    fun escalationRoute(crisisLevel: CrisisLevel, escalationId: String?, sessionId: String?): String {
        val base = "$ESCALATION/${crisisLevel.name}"
        val query = listOfNotNull(
            escalationId?.let { "$ARG_ESCALATION_ID=${Uri.encode(it)}" },
            sessionId?.let { "$ARG_SESSION_ID=${Uri.encode(it)}" }
        ).joinToString("&")
        return if (query.isEmpty()) base else "$base?$query"
    }

    fun callRoute(roomUrl: String, token: String): String =
        "$CALL?$ARG_ROOM_URL=${Uri.encode(roomUrl)}&$ARG_TOKEN=${Uri.encode(token)}"

    fun wellnessDetailRoute(itemId: String): String = "$WELLNESS_DETAIL/${Uri.encode(itemId)}"

    fun consultationDetailRoute(consultationId: String): String = "$CONSULTATION_DETAIL/${Uri.encode(consultationId)}"

    fun chatTranscriptRoute(sessionId: String): String = "$CHAT_TRANSCRIPT/${Uri.encode(sessionId)}"
}

private val MotionEasing = FastOutSlowInEasing

@Composable
fun GabAINavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onStartDestinationResolved: () -> Unit = {}
) {
    val context = LocalContext.current
    val authStore = remember { AuthSessionStore(context) }

    // NavHost needs a startDestination up front, so resolve it from the stored
    // session before composing the graph at all -- without this, every cold
    // start unconditionally rendered RegistrationScreen even with a perfectly
    // valid saved session, which looked exactly like an automatic logout.
    var startDestination by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val hasSession = authStore.session.first() != null
        startDestination = if (hasSession) Routes.CHATBOT else Routes.AUTH
        onStartDestinationResolved()
    }

    val resolvedStartDestination = startDestination
    if (resolvedStartDestination == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Reactively bounce back to the auth flow the moment the stored session
    // disappears out from under an already-running app -- a force-invalidated
    // session (e.g. another device resetting the password) or a failed silent
    // refresh both end in RetrofitClient's authenticator calling
    // clearSession(), but nothing was previously watching for that while the
    // user was already past login, so the app just silently started failing
    // API calls on whatever screen it was on. Centralized here rather than
    // duplicated at each call site (including the manual "Log out" button,
    // which now only needs to clear the session, not navigate itself).
    LaunchedEffect(navController) {
        var hasSeenSession = resolvedStartDestination == Routes.CHATBOT
        authStore.session.collect { session ->
            if (session != null) {
                hasSeenSession = true
            } else if (hasSeenSession) {
                hasSeenSession = false
                navController.navigate(Routes.AUTH) {
                    popUpTo(0)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = resolvedStartDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(350, easing = MotionEasing)) +
                slideInHorizontally(tween(350, easing = MotionEasing)) { it / 4 }
        },
        exitTransition = {
            fadeOut(tween(150, easing = MotionEasing)) +
                slideOutHorizontally(tween(350, easing = MotionEasing)) { -it / 8 }
        },
        popEnterTransition = {
            fadeIn(tween(350, easing = MotionEasing)) +
                slideInHorizontally(tween(350, easing = MotionEasing)) { -it / 4 }
        },
        popExitTransition = {
            fadeOut(tween(150, easing = MotionEasing)) +
                slideOutHorizontally(tween(350, easing = MotionEasing)) { it / 8 }
        }
    ) {
        // Auth screen (Registration)
        composable(Routes.AUTH) {
            RegistrationScreen(
                onRegistrationComplete = { needsLocation ->
                    val destination = if (needsLocation) Routes.COMPLETE_PROFILE else Routes.CHATBOT
                    navController.navigate(destination) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) }
            )
        }

        // Login screen
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginComplete = { needsLocation ->
                    val destination = if (needsLocation) Routes.COMPLETE_PROFILE else Routes.CHATBOT
                    navController.navigate(destination) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.popBackStack() },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        // Post-Google-signup profile completion (province/city)
        composable(Routes.COMPLETE_PROFILE) {
            CompleteProfileScreen(
                onLocationSaved = {
                    navController.navigate(Routes.CHATBOT) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }

        // Forgot password screen
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Dashboard screen
        composable(Routes.DASHBOARD) {
            DashboardScreen(navController = navController)
        }

        // Chatbot screen
        composable(Routes.CHATBOT) {
            ChatbotScreen(navController = navController)
        }

        // Escalation screen
        composable(
            route = Routes.ESCALATION_ROUTE,
            arguments = listOf(
                navArgument(ARG_CRISIS_LEVEL) { type = NavType.StringType },
                navArgument(ARG_ESCALATION_ID) { type = NavType.StringType; nullable = true },
                navArgument(ARG_SESSION_ID) { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val crisisLevel = runCatching {
                CrisisLevel.valueOf(backStackEntry.arguments?.getString(ARG_CRISIS_LEVEL) ?: "NONE")
            }.getOrDefault(CrisisLevel.NONE)
            val escalationId = backStackEntry.arguments?.getString(ARG_ESCALATION_ID)
            val sessionId = backStackEntry.arguments?.getString(ARG_SESSION_ID)

            EscalationScreen(
                crisisLevel = crisisLevel,
                escalationId = escalationId,
                sessionId = sessionId,
                navController = navController
            )
        }

        // Call screen (Daily.co WebView)
        composable(
            route = Routes.CALL_ROUTE,
            arguments = listOf(
                navArgument(ARG_ROOM_URL) { type = NavType.StringType },
                navArgument(ARG_TOKEN) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val roomUrl = backStackEntry.arguments?.getString(ARG_ROOM_URL).orEmpty()
            val token = backStackEntry.arguments?.getString(ARG_TOKEN).orEmpty()

            CallScreen(
                roomUrl = roomUrl,
                token = token,
                navController = navController
            )
        }

        // Consultation screens
        composable(Routes.CONSULTATION) {
            ConsultationListScreen(navController = navController)
        }

        composable(Routes.CONSULTATION_BOOKING) {
            ConsultationBookingScreen(navController = navController)
        }

        composable(
            route = Routes.CONSULTATION_DETAIL_ROUTE,
            arguments = listOf(navArgument(ARG_CONSULTATION_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val consultationId = backStackEntry.arguments?.getString(ARG_CONSULTATION_ID).orEmpty()
            ConsultationDetailScreen(consultationId = consultationId, navController = navController)
        }

        // History screens
        composable(Routes.HISTORY) {
            HistoryScreen(navController = navController)
        }

        composable(
            route = Routes.CHAT_TRANSCRIPT_ROUTE,
            arguments = listOf(navArgument(ARG_SESSION_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString(ARG_SESSION_ID).orEmpty()
            ChatTranscriptScreen(sessionId = sessionId, navController = navController)
        }

        // Nearest Clinics screen
        composable(Routes.NEAREST_CLINICS) {
            NearestClinicsScreen(navController = navController)
        }

        // Settings screen
        composable(Routes.SETTINGS) {
            SettingsScreen(navController = navController)
        }

        // Wellness for Betterness screens
        composable(Routes.WELLNESS) {
            WellnessScreen(navController = navController)
        }

        composable(
            route = Routes.WELLNESS_DETAIL_ROUTE,
            arguments = listOf(navArgument(ARG_WELLNESS_ITEM_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString(ARG_WELLNESS_ITEM_ID).orEmpty()
            WellnessDetailScreen(itemId = itemId, navController = navController)
        }
    }
}

package com.gabai.presentation.navigation

import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gabai.domain.model.CrisisLevel
import com.gabai.presentation.ui.auth.LoginScreen
import com.gabai.presentation.ui.auth.RegistrationScreen
import com.gabai.presentation.ui.call.CallScreen
import com.gabai.presentation.ui.chatbot.ChatbotScreen
import com.gabai.presentation.ui.common.PlaceholderScreen
import com.gabai.presentation.ui.dashboard.DashboardScreen
import com.gabai.presentation.ui.escalation.EscalationScreen

private const val ARG_CRISIS_LEVEL = "crisisLevel"
private const val ARG_ESCALATION_ID = "escalationId"
private const val ARG_SESSION_ID = "sessionId"
private const val ARG_ROOM_URL = "roomUrl"
private const val ARG_TOKEN = "token"

// Navigation routes
object Routes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val CHATBOT = "chatbot"
    const val ESCALATION = "escalation"
    const val CALL = "call"
    const val CONSULTATION = "consultation"
    const val SETTINGS = "settings"

    // escalationId/sessionId are server-issued, so they must be optional query
    // params, not path segments -- LOCAL_KEYWORD/FALLBACK responses that never
    // reached the backend have neither (see ChatViewModel).
    const val ESCALATION_ROUTE =
        "$ESCALATION/{$ARG_CRISIS_LEVEL}?$ARG_ESCALATION_ID={$ARG_ESCALATION_ID}&$ARG_SESSION_ID={$ARG_SESSION_ID}"

    const val CALL_ROUTE = "$CALL?$ARG_ROOM_URL={$ARG_ROOM_URL}&$ARG_TOKEN={$ARG_TOKEN}"

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
}

private val MotionEasing = FastOutSlowInEasing

@Composable
fun GabAINavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.AUTH,
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
                onRegistrationComplete = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) }
            )
        }

        // Login screen
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginComplete = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.popBackStack() }
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

        // Consultation screen
        composable(Routes.CONSULTATION) {
            PlaceholderScreen(title = "Consultation", navController = navController)
        }

        // Settings screen
        composable(Routes.SETTINGS) {
            PlaceholderScreen(title = "Settings", navController = navController)
        }
    }
}

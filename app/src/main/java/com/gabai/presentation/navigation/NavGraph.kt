package com.gabai.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gabai.domain.model.CrisisLevel
import com.gabai.presentation.ui.auth.RegistrationScreen
import com.gabai.presentation.ui.chatbot.ChatbotScreen
import com.gabai.presentation.ui.dashboard.DashboardScreen
import com.gabai.presentation.ui.escalation.EscalationScreen

private const val ARG_CRISIS_LEVEL = "crisisLevel"
private const val ARG_MESSAGE = "encodedMessage"

// Navigation routes
object Routes {
    const val AUTH = "auth"
    const val DASHBOARD = "dashboard"
    const val CHATBOT = "chatbot"
    const val ESCALATION = "escalation"
    const val CONSULTATION = "consultation"
    const val SETTINGS = "settings"

    const val ESCALATION_ROUTE = "$ESCALATION/{$ARG_CRISIS_LEVEL}/{$ARG_MESSAGE}"

    fun escalationRoute(crisisLevel: CrisisLevel, triggeringMessage: String = ""): String =
        "$ESCALATION/${crisisLevel.name}/${Uri.encode(triggeringMessage)}"
}

@Composable
fun GabAINavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.AUTH,
        modifier = modifier
    ) {
        // Auth screen (Registration)
        composable(Routes.AUTH) {
            RegistrationScreen(
                onRegistrationComplete = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
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
                navArgument(ARG_MESSAGE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val crisisLevel = runCatching {
                CrisisLevel.valueOf(backStackEntry.arguments?.getString(ARG_CRISIS_LEVEL) ?: "NONE")
            }.getOrDefault(CrisisLevel.NONE)
            val triggeringMessage = Uri.decode(backStackEntry.arguments?.getString(ARG_MESSAGE) ?: "")

            EscalationScreen(
                crisisLevel = crisisLevel,
                triggeringMessage = triggeringMessage,
                navController = navController
            )
        }

        // Consultation screen
        composable(Routes.CONSULTATION) {
            ConsultationScreen()
        }

        // Settings screen
        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
    }
}

// Placeholder screens - to be implemented
@Composable
fun ConsultationScreen() {
    Text(
        text = "Consultation Screen",
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun SettingsScreen() {
    Text(
        text = "Settings Screen",
        modifier = Modifier.fillMaxSize()
    )
}

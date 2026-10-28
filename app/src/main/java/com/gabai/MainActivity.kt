package com.gabai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.gabai.presentation.navigation.GabAINavGraph
import com.gabai.presentation.theme.GabAITheme
import com.gabai.presentation.util.RequestPermissionsOnFirstLaunch

class MainActivity : ComponentActivity() {

    // The splash stays up until the nav graph has read the stored session and
    // knows whether to open chat or sign-in, so a cold start never flashes a spinner.
    private var startDestinationResolved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !startDestinationResolved }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GabAITheme {
                RequestPermissionsOnFirstLaunch()
                val navController = rememberNavController()
                GabAINavGraph(
                    navController = navController,
                    onStartDestinationResolved = { startDestinationResolved = true }
                )
            }
        }
    }
}
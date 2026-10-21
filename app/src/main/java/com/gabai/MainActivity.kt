package com.gabai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.gabai.presentation.navigation.GabAINavGraph
import com.gabai.presentation.theme.GabAITheme
import com.gabai.presentation.util.RequestPermissionsOnFirstLaunch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GabAITheme {
                RequestPermissionsOnFirstLaunch()
                val navController = rememberNavController()
                GabAINavGraph(navController = navController)
            }
        }
    }
}
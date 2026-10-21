package com.gabai.presentation.util

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

private const val PREFS_NAME = "gabai_first_launch"
private const val KEY_PERMISSIONS_PROMPTED = "permissions_prompted"

private val FIRST_LAUNCH_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

/**
 * Asks for camera, microphone and location the first time the app is opened,
 * so the call and nearest-clinics features work without a surprise prompt
 * mid-use. Shown once only: the "already asked" flag is set before the system
 * dialogs are launched, so rotation or process death can't re-prompt. Declining
 * is fine -- the call and clinics screens still ask again when they need a
 * permission the user hasn't granted.
 */
@Composable
fun RequestPermissionsOnFirstLaunch() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Results are re-checked by each feature at the point of use. */ }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_PERMISSIONS_PROMPTED, false)) {
            prefs.edit().putBoolean(KEY_PERMISSIONS_PROMPTED, true).apply()
            launcher.launch(FIRST_LAUNCH_PERMISSIONS)
        }
    }
}

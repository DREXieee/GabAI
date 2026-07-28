package com.gabai.presentation.theme

import androidx.compose.ui.graphics.Color

// GabAI Brand Colors -- warm coral/cream palette inspired by the "MeditationApp"
// reference mockup (inspiration.jpg). CrisisRed is a deliberately separate,
// unchanged semantic color for SEVERE escalation badges, error states, and the
// Emergency Support banner -- it must never be swapped for a decorative accent,
// so urgency signaling stays distinct from the softer brand palette.
val Coral = Color(0xFFED9455)
val CoralDark = Color(0xFFD97D3D)
val Gold = Color(0xFFF4B740)
val Cream = Color(0xFFFDF6EC)
val Ink = Color(0xFF2B2F55)
val MutedGray = Color(0xFF8B8FA3)

val AccentGreen = Color(0xFF3CB878)
val AccentIndigo = Color(0xFF5C6EDB)
val AccentPink = Color(0xFFF0568C)
val AccentAmber = Coral

val CrisisRed = Color(0xFFEF4444)

// Light Theme Colors
val LightPrimary = Coral
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFFCE3CC)
val LightOnPrimaryContainer = CoralDark

val LightSecondary = AccentIndigo
val LightOnSecondary = Color.White
val LightSecondaryContainer = Color(0xFFE1E4FA)
val LightOnSecondaryContainer = Color(0xFF32398F)

val LightTertiary = AccentGreen
val LightOnTertiary = Color.White
val LightTertiaryContainer = Color(0xFFD7F2E3)
val LightOnTertiaryContainer = Color(0xFF1D6B45)

val LightBackground = Cream
val LightOnBackground = Ink
val LightSurface = Color.White
val LightOnSurface = Ink
val LightSurfaceVariant = Color(0xFFF6EEE1)
val LightOnSurfaceVariant = MutedGray

val LightError = CrisisRed
val LightOnError = Color.White
val LightErrorContainer = Color(0xFFFEE2E2)
val LightOnErrorContainer = Color(0xFF7F1D1D)

val LightOutline = Color(0xFFE3D9C8)

// Dark Theme Colors
val DarkPrimary = Coral
val DarkOnPrimary = Color(0xFF4A2A0F)
val DarkPrimaryContainer = CoralDark
val DarkOnPrimaryContainer = Color(0xFFFCE3CC)

val DarkSecondary = Color(0xFF9AA4EE)
val DarkOnSecondary = Color(0xFF23285F)
val DarkSecondaryContainer = Color(0xFF3A4099)
val DarkOnSecondaryContainer = Color(0xFFE1E4FA)

val DarkTertiary = Color(0xFF6FD9A3)
val DarkOnTertiary = Color(0xFF0D3D25)
val DarkTertiaryContainer = Color(0xFF1D6B45)
val DarkOnTertiaryContainer = Color(0xFFD7F2E3)

val DarkBackground = Color(0xFF1E1B18)
val DarkOnBackground = Color(0xFFF4EEE4)
val DarkSurface = Color(0xFF29241F)
val DarkOnSurface = Color(0xFFF4EEE4)
val DarkSurfaceVariant = Color(0xFF3B342C)
val DarkOnSurfaceVariant = Color(0xFFC8BFAF)

val DarkError = Color(0xFFF87171)
val DarkOnError = Color(0xFF7F1D1D)
val DarkErrorContainer = Color(0xFF7F1D1D)
val DarkOnErrorContainer = Color(0xFFFEE2E2)

val DarkOutline = Color(0xFF544C41)

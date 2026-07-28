package com.gabai.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Calming = soft, generous, consistent rounding - slightly larger than stock
// Material3 defaults for a friendlier feel. The chat bubble's asymmetric "tail"
// shape and circular icon badges are deliberate one-offs, not part of this scale.
val GabAIShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// True pill/full-round shape for chips, tags, and pill buttons -- mirrors the
// reference mockup's rounded filter chips. Not part of M3's Shapes scale (which
// tops out at extraLarge's fixed 28dp corner), so exposed as a standalone token.
val PillShape = CircleShape

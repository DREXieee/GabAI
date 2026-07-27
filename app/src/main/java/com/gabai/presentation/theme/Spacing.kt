package com.gabai.presentation.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

object Dimens {
    // Spacing scale (4dp base grid)
    val SpaceXXS = 4.dp
    val SpaceXS = 8.dp
    val SpaceS = 12.dp
    val SpaceM = 16.dp
    val SpaceL = 24.dp
    val SpaceXL = 32.dp
    val SpaceXXL = 48.dp

    // Named component sizes
    val IconSizeM = 28.dp
    val IconSizeL = 32.dp
    val IconBadgeSize = 56.dp
    val ActionCardHeight = 100.dp
    val ButtonHeight = 56.dp

    val ChatBubbleMaxWidthCompact = 280.dp
    val ChatBubbleMaxWidthWide = 420.dp

    val ReadingMaxWidth = 640.dp
}

/**
 * Canonical screen-content padding: consumes Scaffold insets, then applies the
 * shared horizontal rhythm. Every screen should use this instead of hand-rolling
 * its own padding stack.
 */
fun Modifier.screenContentPadding(paddingValues: PaddingValues): Modifier =
    this
        .padding(paddingValues)
        .padding(horizontal = Dimens.SpaceL)

enum class WindowWidthSize { COMPACT, MEDIUM, EXPANDED }

@Composable
fun rememberWindowWidthSize(): WindowWidthSize {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return remember(widthDp) {
        when {
            widthDp < 600 -> WindowWidthSize.COMPACT
            widthDp < 840 -> WindowWidthSize.MEDIUM
            else -> WindowWidthSize.EXPANDED
        }
    }
}

package com.gabai.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAIShapes
import com.gabai.presentation.theme.PillShape
import com.gabai.presentation.util.rememberReducedMotion
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

// Softer than the library default (which dims blocks to 25% between sweeps),
// so the placeholder gently brightens rather than flashing.
private val CalmShimmerTheme = defaultShimmerTheme.copy(
    shaderColors = listOf(
        Color.White.copy(alpha = 0.5f),
        Color.White.copy(alpha = 1f),
        Color.White.copy(alpha = 0.5f)
    )
)

/**
 * Placeholder cards shown while a list's first page loads, in place of a
 * full-screen spinner, shaped like the History / Consultations / Clinics rows.
 * The shimmer sweep is decorative, so with system animations off the blocks
 * render static instead (see the motion-and-reduced-motion skill).
 */
@Composable
fun SkeletonList(
    loadingDescription: String,
    modifier: Modifier = Modifier,
    rowCount: Int = 6,
    showLeadingBadge: Boolean = true
) {
    val reducedMotion = rememberReducedMotion()
    // One shimmer bounded to the window, so a single sweep crosses every block
    // in step instead of each block running its own.
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window, theme = CalmShimmerTheme)
    val blockModifier = if (reducedMotion) Modifier else Modifier.shimmer(shimmer)
    val blockColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = loadingDescription },
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
    ) {
        repeat(rowCount) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = GabAIShapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpaceM),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showLeadingBadge) {
                        Box(
                            blockModifier
                                .size(Dimens.IconBadgeSize)
                                .background(blockColor, CircleShape)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)) {
                        Box(
                            blockModifier
                                .fillMaxWidth(0.6f)
                                .height(Dimens.SpaceM)
                                .background(blockColor, PillShape)
                        )
                        Box(
                            blockModifier
                                .fillMaxWidth(0.4f)
                                .height(Dimens.SpaceS)
                                .background(blockColor, PillShape)
                        )
                    }
                }
            }
        }
    }
}

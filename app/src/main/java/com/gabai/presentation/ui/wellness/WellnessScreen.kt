package com.gabai.presentation.ui.wellness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gabai.domain.model.WellnessCategory
import com.gabai.domain.model.WellnessItem
import com.gabai.domain.model.WellnessLibrary
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.GabAIShapes
import com.gabai.presentation.theme.LocalGabAIAccents
import com.gabai.presentation.theme.screenContentPadding
import androidx.compose.ui.res.painterResource
import com.gabai.R
import androidx.annotation.DrawableRes
import com.gabai.presentation.ui.common.GabAILogoButton

// Same tone per category everywhere a Wellness item shows: breathing green,
// coping tips pink, articles indigo.
@Composable
internal fun WellnessCategory.accentColor(): Color {
    val accents = LocalGabAIAccents.current
    return when (this) {
        WellnessCategory.BREATHING -> accents.green
        WellnessCategory.COPING_TIPS -> accents.pink
        WellnessCategory.ARTICLES -> accents.indigo
    }
}

@DrawableRes
internal fun WellnessCategory.iconRes(): Int = when (this) {
    WellnessCategory.BREATHING -> R.drawable.ic_lucide_wind
    WellnessCategory.COPING_TIPS -> R.drawable.ic_lucide_lightbulb
    WellnessCategory.ARTICLES -> R.drawable.ic_lucide_file_text
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val viewModel: WellnessViewModel = viewModel(factory = WellnessViewModelFactory(context))
    val suggested by viewModel.suggested.collectAsState()
    val grouped = WellnessLibrary.items.groupBy { it.category }

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wellness for Betterness", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { GabAILogoButton(navController) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
        ) {
            if (suggested.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = Dimens.SpaceM, bottom = Dimens.SpaceXS)) {
                        Text(
                            text = "Suggested for you",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Based on how you've been feeling in your recent chats.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(suggested) { wellnessItem ->
                    WellnessItemCard(
                        item = wellnessItem,
                        accentColor = wellnessItem.category.accentColor(),
                        icon = wellnessItem.category.iconRes(),
                        onClick = { navController?.navigate(Routes.wellnessDetailRoute(wellnessItem.id)) }
                    )
                }
            }
            WellnessCategory.entries.forEach { category ->
                val categoryItems = grouped[category].orEmpty()
                if (categoryItems.isNotEmpty()) {
                    item {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = Dimens.SpaceM, bottom = Dimens.SpaceXS)
                        )
                    }
                    items(categoryItems) { wellnessItem ->
                        WellnessItemCard(
                            item = wellnessItem,
                            accentColor = category.accentColor(),
                            icon = category.iconRes(),
                            onClick = { navController?.navigate(Routes.wellnessDetailRoute(wellnessItem.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WellnessItemCard(
    item: WellnessItem,
    accentColor: androidx.compose.ui.graphics.Color,
    @DrawableRes icon: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = GabAIShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.IconBadgeSize)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(icon), contentDescription = null, tint = accentColor)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_lucide_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

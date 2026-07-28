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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessScreen(navController: NavController? = null) {
    val accents = LocalGabAIAccents.current
    val grouped = WellnessLibrary.items.groupBy { it.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Wellness", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
        ) {
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
                            accentColor = when (category) {
                                WellnessCategory.BREATHING -> accents.green
                                WellnessCategory.COPING_TIPS -> accents.pink
                                WellnessCategory.ARTICLES -> accents.indigo
                            },
                            icon = when (category) {
                                WellnessCategory.BREATHING -> Icons.Filled.Air
                                WellnessCategory.COPING_TIPS -> Icons.Filled.Lightbulb
                                WellnessCategory.ARTICLES -> Icons.AutoMirrored.Filled.Article
                            },
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
    icon: ImageVector,
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
                Icon(imageVector = icon, contentDescription = null, tint = accentColor)
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
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

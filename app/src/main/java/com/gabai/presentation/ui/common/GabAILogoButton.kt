package com.gabai.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gabai.R
import com.gabai.presentation.navigation.Routes
import com.gabai.presentation.theme.Dimens

/** Goes to the dashboard, reusing the existing entry when there is one. */
fun navigateToMenu(navController: NavController?) {
    if (navController?.popBackStack(Routes.DASHBOARD, inclusive = false) != true) {
        navController?.navigate(Routes.DASHBOARD) { launchSingleTop = true }
    }
}

/**
 * The GabAI logo as a top-bar navigation button: it replaces the back arrow on
 * screens opened from the dashboard and takes the user to the dashboard (the
 * menu), the same way tapping the logo in the chat's top bar does. Returns to
 * the existing dashboard entry when there is one, so it never stacks a second
 * copy on top.
 */
@Composable
fun GabAILogoButton(
    navController: NavController?,
    modifier: Modifier = Modifier,
    // Lets a screen intercept the tap (e.g. to confirm before leaving); the
    // default goes straight to the menu.
    onClick: () -> Unit = { navigateToMenu(navController) }
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                onClickLabel = "Open menu",
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo_gabai),
            contentDescription = "GabAI menu",
            modifier = Modifier.size(Dimens.IconBadgeSize - Dimens.SpaceM)
        )
    }
}

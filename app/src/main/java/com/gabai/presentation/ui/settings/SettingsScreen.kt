package com.gabai.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gabai.data.local.repository.AuthSessionStore
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.screenContentPadding
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import com.gabai.R
import com.gabai.presentation.ui.common.GabAILogoButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authStore = remember { AuthSessionStore(context) }

    var email by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authStore.session.collect { session ->
            email = session?.email.orEmpty()
            displayName = session?.displayName.orEmpty()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { GabAILogoButton(navController) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues)
                .padding(horizontal = Dimens.SpaceM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
        ) {
            Spacer(modifier = Modifier.height(Dimens.SpaceS))

            if (displayName.isNotBlank() || email.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXS)) {
                    if (displayName.isNotBlank()) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (email.isNotBlank()) {
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider()
            }

            OutlinedButton(
                onClick = {
                    // Just clears the session -- GabAINavGraph reactively
                    // watches for that and handles the redirect to Routes.AUTH
                    // itself, the same way it now does for a forced logout.
                    coroutineScope.launch {
                        authStore.clearSession()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = ButtonDefaults.outlinedButtonBorder(true).copy(
                    width = 1.dp
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_log_out),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconSizeM)
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceS))
                Text("Log out", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

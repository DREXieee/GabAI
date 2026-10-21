package com.gabai.presentation.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gabai.presentation.theme.Dimens
import com.gabai.presentation.theme.screenContentPadding
import com.gabai.presentation.ui.chatbot.ChatBubble
import com.gabai.presentation.ui.common.GabAILogoButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTranscriptScreen(sessionId: String, navController: NavController? = null) {
    val context = LocalContext.current
    val viewModel: ChatTranscriptViewModel = viewModel(factory = ChatTranscriptViewModelFactory(context))

    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(sessionId) { viewModel.load(sessionId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversation", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { GabAILogoButton(navController) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .screenContentPadding(paddingValues)
        ) {
            when {
                isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(vertical = Dimens.SpaceM),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatBubble(message = message)
                    }
                }
            }
        }
    }
}

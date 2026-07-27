package com.gabai.presentation.ui.chatbot

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.domain.model.ChatMessage
import com.gabai.domain.model.CrisisLevel
import com.gabai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = mutableStateListOf(
        ChatMessage(
            id = "welcome",
            text = "Hello! I'm GabAI, your mental wellness assistant. How are you feeling today?",
            isUser = false
        )
    )
    val messages: List<ChatMessage> get() = _messages

    private val _crisisLevel = MutableStateFlow(CrisisLevel.NONE)
    val crisisLevel: StateFlow<CrisisLevel> = _crisisLevel.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    // Null until the first message reaches the backend (unset for a purely
    // local LOCAL_KEYWORD/FALLBACK response, which never got a real session).
    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    // Set only when a message auto-escalates (SEVERE); tracks the most recent
    // such escalation so "Speak to a Professional" can jump straight to its
    // real status instead of creating a duplicate one.
    private val _escalationId = MutableStateFlow<String?>(null)
    val escalationId: StateFlow<String?> = _escalationId.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        _messages.add(ChatMessage(id = System.currentTimeMillis().toString(), text = text, isUser = true))

        viewModelScope.launch {
            _isTyping.value = true
            val assessment = chatRepository.sendMessage(text)
            _isTyping.value = false

            _messages.add(
                ChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    text = assessment.replyText,
                    isUser = false,
                    crisisLevel = assessment.crisisLevel
                )
            )

            assessment.sessionId?.let { _sessionId.value = it }
            assessment.escalationId?.let { _escalationId.value = it }

            // Crisis level only escalates for the rest of this session - never silently
            // downgraded by a later, calmer message.
            if (assessment.crisisLevel.ordinal > _crisisLevel.value.ordinal) {
                _crisisLevel.value = assessment.crisisLevel
            }
        }
    }
}

class ChatViewModelFactory(
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChatViewModel(chatRepository) as T
    }
}

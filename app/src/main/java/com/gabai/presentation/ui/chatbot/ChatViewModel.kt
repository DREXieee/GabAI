package com.gabai.presentation.ui.chatbot

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.domain.model.ChatMessage
import com.gabai.domain.model.CrisisAssessment
import com.gabai.domain.model.CrisisLevel
import com.gabai.domain.repository.ChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    // Sends still waiting on a reply -- the repository queues them one at a
    // time, so the first one finishing mustn't hide the indicator for the next.
    private var awaitingReplies = 0

    // Null until the first message reaches the backend (unset for a purely
    // local LOCAL_KEYWORD/FALLBACK response, which never got a real session).
    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    // Set only when a message auto-escalates (SEVERE); tracks the most recent
    // such escalation so "Speak to a Professional" can jump straight to its
    // real status instead of creating a duplicate one.
    private val _escalationId = MutableStateFlow<String?>(null)
    val escalationId: StateFlow<String?> = _escalationId.asStateFlow()

    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _voiceError = MutableStateFlow<String?>(null)
    val voiceError: StateFlow<String?> = _voiceError.asStateFlow()

    // One-shot events rather than a StateFlow -- a StateFlow would replay the
    // last clip and re-trigger playback on every collector restart (e.g. a
    // config change), which a SharedFlow with no replay avoids.
    private val _speechEvents = MutableSharedFlow<ByteArray>(extraBufferCapacity = 1)
    val speechEvents: SharedFlow<ByteArray> = _speechEvents.asSharedFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        _messages.add(ChatMessage(id = System.currentTimeMillis().toString(), text = text, isUser = true))

        // A SEVERE keyword match is answered (and raises the banner + hotlines)
        // right away instead of after the backend call, which can take 30-60s to
        // wake on Render's free tier. The message is still sent below: the
        // backend is what saves it to History and auto-escalates it to staff.
        val localAssessment = chatRepository.localCrisisAssessment(text)
        localAssessment?.let {
            addReply(it)
            raiseCrisisLevel(it.crisisLevel)
        }

        viewModelScope.launch {
            if (localAssessment == null) _isTyping.value = ++awaitingReplies > 0
            val assessment = chatRepository.sendMessage(text)
            if (localAssessment == null) _isTyping.value = --awaitingReplies > 0

            // The backend runs the same keyword check and returns this same
            // reply, so only add one that says something the local reply didn't.
            if (assessment.replyText != localAssessment?.replyText) addReply(assessment)

            assessment.sessionId?.let { _sessionId.value = it }
            assessment.escalationId?.let { _escalationId.value = it }
            raiseCrisisLevel(assessment.crisisLevel)
        }
    }

    private fun addReply(assessment: CrisisAssessment) {
        _messages.add(
            ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                text = assessment.replyText,
                isUser = false,
                crisisLevel = assessment.crisisLevel,
                emotion = assessment.emotion
            )
        )
        speak(assessment.replyText)
    }

    // Crisis level only escalates for the rest of this session - never silently
    // downgraded by a later, calmer message.
    private fun raiseCrisisLevel(level: CrisisLevel) {
        if (level.ordinal > _crisisLevel.value.ordinal) {
            _crisisLevel.value = level
        }
    }

    // Read aloud automatically -- for a patient too shaken to read the
    // screen, hearing the response matters as much as being able to speak
    // the message in. Best-effort: a TTS failure must never surface as an
    // error on top of an already-successful text reply.
    private fun speak(text: String) {
        viewModelScope.launch {
            try {
                _speechEvents.emit(chatRepository.synthesizeSpeech(text))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Silent by design -- see comment above.
            }
        }
    }

    // Push-to-talk voice input: the caller (ChatbotScreen) owns the actual
    // recording (MediaRecorder needs a Context/lifecycle the ViewModel
    // shouldn't hold); this just transcribes and, on success, sends the
    // result through the exact same path as a typed message.
    fun sendVoiceMessage(audioBytes: ByteArray, mimeType: String) {
        viewModelScope.launch {
            _isTranscribing.value = true
            _voiceError.value = null
            try {
                val text = chatRepository.transcribeAudio(audioBytes, mimeType)
                _isTranscribing.value = false
                if (text.isNotBlank()) sendMessage(text)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _isTranscribing.value = false
                _voiceError.value = "Couldn't hear that clearly. Please try again or type your message."
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

package com.gabai.presentation.ui.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.data.remote.api.RemoteHistoryRepositoryImpl
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.domain.model.ChatMessage
import com.gabai.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatTranscriptViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun load(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _messages.value = historyRepository.getChatSessionTranscript(sessionId)
            } catch (e: Exception) {
                _error.value = "Couldn't load this conversation. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class ChatTranscriptViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = RemoteHistoryRepositoryImpl(RetrofitClient.getApiService(appContext.applicationContext))
        @Suppress("UNCHECKED_CAST")
        return ChatTranscriptViewModel(repository) as T
    }
}

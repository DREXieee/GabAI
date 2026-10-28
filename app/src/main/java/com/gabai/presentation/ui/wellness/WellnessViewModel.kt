package com.gabai.presentation.ui.wellness

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.data.remote.api.BackendChatRepositoryImpl
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.domain.model.WellnessItem
import com.gabai.domain.model.WellnessLibrary
import com.gabai.domain.repository.ChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WellnessViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _suggested = MutableStateFlow<List<WellnessItem>>(emptyList())
    val suggested: StateFlow<List<WellnessItem>> = _suggested.asStateFlow()

    // "Suggested for you" from the emotions tagged in recent chats. Optional:
    // with no chats yet, or the request failing, the screen is just the library.
    fun refresh() {
        viewModelScope.launch {
            _suggested.value = try {
                WellnessLibrary.suggestionsFor(chatRepository.recentEmotions())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

class WellnessViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = BackendChatRepositoryImpl(RetrofitClient.getApiService(appContext.applicationContext))
        @Suppress("UNCHECKED_CAST")
        return WellnessViewModel(repository) as T
    }
}

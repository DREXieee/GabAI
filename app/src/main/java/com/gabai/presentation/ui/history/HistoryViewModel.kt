package com.gabai.presentation.ui.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.data.remote.api.RemoteHistoryRepositoryImpl
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.domain.model.HistoryItem
import com.gabai.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<HistoryItem>>(emptyList())
    val items: StateFlow<List<HistoryItem>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var nextCursor: String? = null
    private var endReached = false

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val page = historyRepository.getHistory(cursor = null)
                _items.value = page.items
                nextCursor = page.nextCursor
                endReached = page.nextCursor == null
            } catch (e: Exception) {
                _error.value = "Couldn't load your history. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value || endReached) return
        val cursor = nextCursor ?: return
        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val page = historyRepository.getHistory(cursor = cursor)
                _items.value = _items.value + page.items
                nextCursor = page.nextCursor
                endReached = page.nextCursor == null
            } catch (e: Exception) {
                // Silent -- existing items stay visible; scrolling back near the
                // end will simply retry since loadMore() isn't marked exhausted.
            } finally {
                _isLoadingMore.value = false
            }
        }
    }
}

class HistoryViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = RemoteHistoryRepositoryImpl(RetrofitClient.getApiService(appContext.applicationContext))
        @Suppress("UNCHECKED_CAST")
        return HistoryViewModel(repository) as T
    }
}

package com.gabai.presentation.ui.consultation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.data.remote.api.RemoteConsultationRepositoryImpl
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.domain.repository.ConsultationRepository
import com.gabai.domain.repository.ConsultationSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConsultationListViewModel(
    private val consultationRepository: ConsultationRepository
) : ViewModel() {

    private val _consultations = MutableStateFlow<List<ConsultationSummary>>(emptyList())
    val consultations: StateFlow<List<ConsultationSummary>> = _consultations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _consultations.value = consultationRepository.listMyConsultations()
            } catch (e: Exception) {
                _error.value = "Couldn't load your consultations. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class ConsultationListViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = RemoteConsultationRepositoryImpl(RetrofitClient.getApiService(appContext.applicationContext))
        @Suppress("UNCHECKED_CAST")
        return ConsultationListViewModel(repository) as T
    }
}

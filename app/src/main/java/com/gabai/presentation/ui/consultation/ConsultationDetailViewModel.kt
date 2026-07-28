package com.gabai.presentation.ui.consultation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.data.remote.api.RemoteConsultationRepositoryImpl
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.domain.repository.ConsultationRepository
import com.gabai.domain.repository.ConsultationStatusUpdate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConsultationDetailViewModel(
    private val consultationRepository: ConsultationRepository
) : ViewModel() {

    private val _status = MutableStateFlow<ConsultationStatusUpdate?>(null)
    val status: StateFlow<ConsultationStatusUpdate?> = _status.asStateFlow()

    private val _isCancelling = MutableStateFlow(false)
    val isCancelling: StateFlow<Boolean> = _isCancelling.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var pollingJob: Job? = null

    fun observe(consultationId: String) {
        if (pollingJob != null) return
        pollingJob = viewModelScope.launch {
            try {
                consultationRepository.observeConsultationStatus(consultationId).collect { update ->
                    _status.value = update
                }
            } catch (e: Exception) {
                _error.value = "Lost connection while checking status. Please try again shortly."
            }
        }
    }

    fun cancel(consultationId: String) {
        if (_isCancelling.value) return
        viewModelScope.launch {
            _isCancelling.value = true
            _error.value = null
            try {
                consultationRepository.cancelConsultation(consultationId)
                pollingJob?.cancel()
                pollingJob = null
                observe(consultationId)
            } catch (e: Exception) {
                _error.value = "Couldn't cancel this consultation. Please try again."
            } finally {
                _isCancelling.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

class ConsultationDetailViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = RemoteConsultationRepositoryImpl(RetrofitClient.getApiService(appContext.applicationContext))
        @Suppress("UNCHECKED_CAST")
        return ConsultationDetailViewModel(repository) as T
    }
}

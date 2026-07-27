package com.gabai.presentation.ui.escalation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.data.remote.api.RemoteEscalationRepositoryImpl
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.domain.model.CrisisLevel
import com.gabai.domain.repository.EscalationRepository
import com.gabai.domain.repository.EscalationStatusUpdate
import com.gabai.domain.repository.RemoteEscalationStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// If nobody claims within this window, stop making the user stare at a spinner
// and surface the hotline resources as the primary path instead.
private const val WAIT_TIMEOUT_MS = 90_000L

class EscalationViewModel(
    private val escalationRepository: EscalationRepository
) : ViewModel() {

    private val _status = MutableStateFlow<EscalationStatusUpdate?>(null)
    val status: StateFlow<EscalationStatusUpdate?> = _status.asStateFlow()

    private val _isRequesting = MutableStateFlow(false)
    val isRequesting: StateFlow<Boolean> = _isRequesting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _waitTimedOut = MutableStateFlow(false)
    val waitTimedOut: StateFlow<Boolean> = _waitTimedOut.asStateFlow()

    private var pollingJob: Job? = null
    private var timeoutJob: Job? = null

    /** An escalation already exists (server auto-created it on a SEVERE reply) — just watch it. */
    fun observeExisting(escalationId: String) {
        startPolling(escalationId)
    }

    /** The patient is explicitly asking for a human — create the escalation, then watch it. */
    fun requestHumanSupport(sessionId: String, crisisLevel: CrisisLevel) {
        if (_isRequesting.value || pollingJob != null) return
        viewModelScope.launch {
            _isRequesting.value = true
            _error.value = null
            try {
                val escalationId = escalationRepository.requestHumanSupport(sessionId, crisisLevel)
                startPolling(escalationId)
            } catch (e: Exception) {
                _error.value = "Couldn't reach GabAI. Please use the resources below to get help right now."
            } finally {
                _isRequesting.value = false
            }
        }
    }

    private fun startPolling(escalationId: String) {
        pollingJob?.cancel()
        timeoutJob?.cancel()
        _waitTimedOut.value = false

        timeoutJob = viewModelScope.launch {
            delay(WAIT_TIMEOUT_MS)
            if (_status.value?.status == RemoteEscalationStatus.PENDING) {
                _waitTimedOut.value = true
            }
        }

        pollingJob = viewModelScope.launch {
            try {
                escalationRepository.observeEscalationStatus(escalationId).collect { update ->
                    _status.value = update
                    if (update.status != RemoteEscalationStatus.PENDING) {
                        timeoutJob?.cancel()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Lost connection while checking status. Please use the resources below to get help right now."
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        timeoutJob?.cancel()
    }
}

class EscalationViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = RemoteEscalationRepositoryImpl(RetrofitClient.getApiService(appContext.applicationContext))
        @Suppress("UNCHECKED_CAST")
        return EscalationViewModel(repository) as T
    }
}

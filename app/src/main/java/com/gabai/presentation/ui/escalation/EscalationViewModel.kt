package com.gabai.presentation.ui.escalation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.data.local.repository.EscalationRepositoryImpl
import com.gabai.domain.model.CrisisLevel
import com.gabai.domain.repository.EscalationConfirmation
import com.gabai.domain.repository.EscalationEvent
import com.gabai.domain.repository.EscalationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EscalationViewModel(
    private val escalationRepository: EscalationRepository
) : ViewModel() {

    private val _confirmation = MutableStateFlow<EscalationConfirmation?>(null)
    val confirmation: StateFlow<EscalationConfirmation?> = _confirmation.asStateFlow()

    fun requestHumanSupport(crisisLevel: CrisisLevel, triggeringMessage: String) {
        viewModelScope.launch {
            val event = EscalationEvent(
                id = System.currentTimeMillis().toString(),
                crisisLevel = crisisLevel,
                triggeringMessage = triggeringMessage,
                timestampEpochMillis = System.currentTimeMillis(),
                requestedHumanSupport = true
            )
            _confirmation.value = escalationRepository.requestHumanSupport(event)
        }
    }
}

class EscalationViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EscalationViewModel(EscalationRepositoryImpl(appContext.applicationContext)) as T
    }
}

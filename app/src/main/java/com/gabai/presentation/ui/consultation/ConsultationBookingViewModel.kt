package com.gabai.presentation.ui.consultation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.data.remote.api.RemoteConsultationRepositoryImpl
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.domain.repository.AvailableSlot
import com.gabai.domain.repository.ConsultationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ConsultationBookingViewModel(
    private val consultationRepository: ConsultationRepository
) : ViewModel() {

    private val _slots = MutableStateFlow<List<AvailableSlot>>(emptyList())
    val slots: StateFlow<List<AvailableSlot>> = _slots.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isBooking = MutableStateFlow(false)
    val isBooking: StateFlow<Boolean> = _isBooking.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _bookedConsultationId = MutableStateFlow<String?>(null)
    val bookedConsultationId: StateFlow<String?> = _bookedConsultationId.asStateFlow()

    fun loadSlots() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _slots.value = consultationRepository.listOpenSlots()
            } catch (e: Exception) {
                _error.value = "Couldn't load available times. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun bookSlot(slotId: String, reasonNote: String?) {
        if (_isBooking.value) return
        viewModelScope.launch {
            _isBooking.value = true
            _error.value = null
            try {
                _bookedConsultationId.value = consultationRepository.bookSlot(slotId, reasonNote?.ifBlank { null })
            } catch (e: HttpException) {
                if (e.code() == 409) {
                    _error.value = "That time was just booked by someone else. Please pick another."
                    loadSlots()
                } else {
                    _error.value = "Couldn't book this consultation. Please try again."
                }
            } catch (e: Exception) {
                _error.value = "Couldn't book this consultation. Please try again."
            } finally {
                _isBooking.value = false
            }
        }
    }
}

class ConsultationBookingViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = RemoteConsultationRepositoryImpl(RetrofitClient.getApiService(appContext.applicationContext))
        @Suppress("UNCHECKED_CAST")
        return ConsultationBookingViewModel(repository) as T
    }
}

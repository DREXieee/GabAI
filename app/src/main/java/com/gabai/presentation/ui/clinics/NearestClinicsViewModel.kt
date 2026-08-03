package com.gabai.presentation.ui.clinics

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.data.remote.api.RemoteClinicRepositoryImpl
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.domain.model.Clinic
import com.gabai.domain.repository.ClinicRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserLocation(val latitude: Double, val longitude: Double)

data class ClinicWithDistance(
    val clinic: Clinic,
    val distanceKm: Double?
)

private const val EARTH_RADIUS_KM = 6371.0

class NearestClinicsViewModel(
    private val clinicRepository: ClinicRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _clinics = MutableStateFlow<List<ClinicWithDistance>>(emptyList())
    val clinics: StateFlow<List<ClinicWithDistance>> = _clinics.asStateFlow()

    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var loadedClinics: List<Clinic> = emptyList()

    init {
        loadClinics()
    }

    private fun loadClinics() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                loadedClinics = clinicRepository.listClinics()
                recomputeDistances()
            } catch (e: Exception) {
                _error.value = "Couldn't load nearby clinics. Check your connection and try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Permission itself is requested/tracked by the screen (mirrors CallScreen's
    // pattern) -- this is only called once it's already been granted.
    @SuppressLint("MissingPermission")
    fun onLocationPermissionGranted() {
        viewModelScope.launch {
            try {
                val cancellationTokenSource = CancellationTokenSource()
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token
                ).await()
                if (location != null) {
                    _userLocation.value = UserLocation(location.latitude, location.longitude)
                    recomputeDistances()
                }
            } catch (e: Exception) {
                // Location is a nice-to-have here -- keep showing the unsorted clinic list.
            }
        }
    }

    private fun recomputeDistances() {
        val loc = _userLocation.value
        val withDistance = loadedClinics.map { clinic ->
            val distance = loc?.let { haversineKm(it.latitude, it.longitude, clinic.latitude, clinic.longitude) }
            ClinicWithDistance(clinic, distance)
        }
        _clinics.value = if (loc != null) {
            withDistance.sortedBy { it.distanceKm }
        } else {
            withDistance
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }
}

class NearestClinicsViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = RemoteClinicRepositoryImpl(RetrofitClient.getApiService(appContext.applicationContext))
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext.applicationContext)
        @Suppress("UNCHECKED_CAST")
        return NearestClinicsViewModel(repository, fusedLocationClient) as T
    }
}

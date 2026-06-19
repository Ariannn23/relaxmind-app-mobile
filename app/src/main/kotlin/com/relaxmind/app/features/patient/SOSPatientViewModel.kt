package com.relaxmind.app.features.patient

import android.annotation.SuppressLint
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.relaxmind.app.data.model.CaregiverAlert
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SOSPatientViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val authService: FirebaseAuthService = FirebaseAuthService(),
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SOSPatientUiState())
    val uiState: StateFlow<SOSPatientUiState> = _uiState.asStateFlow()

    private var currentAlertId: String? = null
    private var locationCallback: LocationCallback? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val patientId = authService.getCurrentUser()?.uid ?: return@launch
            val patientResult = firestoreRepository.getPatientById(patientId)
            val patient = patientResult.getOrNull() ?: return@launch

            val caregiverId = patient.caregiverId
            var caregiverPhone = ""
            if (caregiverId != null) {
                val caregiverResult = firestoreRepository.getCaregiverById(caregiverId)
                caregiverPhone = caregiverResult.getOrNull()?.phone ?: ""
            }

            _uiState.value = _uiState.value.copy(
                patientId = patient.id,
                patientName = "${patient.name} ${patient.lastName}",
                caregiverId = caregiverId,
                caregiverPhone = caregiverPhone,
                isDataLoaded = true
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun activateSOS() {
        if (_uiState.value.isSOSActive || !_uiState.value.isDataLoaded) return

        val state = _uiState.value
        val caregiverId = state.caregiverId
        if (caregiverId == null) {
            _uiState.value = state.copy(error = "No tienes ningún cuidador vinculado.")
            return
        }

        _uiState.value = state.copy(isSOSActive = true)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateLocation(location.latitude, location.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun updateLocation(lat: Double, lng: Double) {
        val alertId = currentAlertId
        if (alertId == null) {
            val state = _uiState.value
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val newAlert = CaregiverAlert(
                caregiverId = state.caregiverId ?: "",
                patientId = state.patientId,
                patientName = state.patientName,
                type = "sos",
                title = "Alerta SOS",
                message = "El paciente ha activado el botón de emergencia.",
                severity = "critical",
                resolved = false,
                latitude = lat,
                longitude = lng,
                createdAtText = dateFormat.format(Date())
            )

            viewModelScope.launch {
                val result = firestoreRepository.createAlert(newAlert)
                if (result.isSuccess) {
                    currentAlertId = result.getOrNull()
                }
            }
        } else {
            viewModelScope.launch {
                firestoreRepository.updateAlertLocation(alertId, lat, lng)
            }
        }
    }

    fun cancelSOS() {
        val alertId = currentAlertId
        if (alertId != null) {
            viewModelScope.launch {
                firestoreRepository.updateAlertResolved(alertId, true)
            }
        }
        stopLocationUpdates()
        _uiState.value = _uiState.value.copy(isSOSActive = false)
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}

data class SOSPatientUiState(
    val isDataLoaded: Boolean = false,
    val isSOSActive: Boolean = false,
    val patientId: String = "",
    val patientName: String = "",
    val caregiverId: String? = null,
    val caregiverPhone: String = "",
    val error: String? = null
)

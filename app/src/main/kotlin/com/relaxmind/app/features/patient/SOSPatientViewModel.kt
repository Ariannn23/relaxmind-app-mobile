package com.relaxmind.app.features.patient

import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
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
import com.relaxmind.app.data.remote.NotificationApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.relaxmind.app.utils.toUserFriendlyMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SOSPatientViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val authService: FirebaseAuthService = FirebaseAuthService(),
    private val notificationApiService: NotificationApiService = NotificationApiService(),
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SOSPatientUiState())
    val uiState: StateFlow<SOSPatientUiState> = _uiState.asStateFlow()

    private var currentAlertId: String? = null
    private var locationCallback: LocationCallback? = null
    private var isCreatingAlert = false

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
            var caregiverName = ""
            if (caregiverId != null) {
                val caregiverResult = firestoreRepository.getCaregiverById(caregiverId)
                val caregiver = caregiverResult.getOrNull()
                caregiverPhone = caregiver?.phone ?: ""
                caregiverName = if (caregiver != null) "${caregiver.name} ${caregiver.lastName}".trim() else ""
            }

            _uiState.value = _uiState.value.copy(
                patientId = patient.id,
                patientName = "${patient.name} ${patient.lastName}",
                caregiverId = caregiverId,
                caregiverPhone = caregiverPhone,
                caregiverName = caregiverName,
                notificationsEnabled = patient.notificationsEnabled,
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
        createOrUpdateAlert(latitude = null, longitude = null)

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

        runCatching {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        }.onFailure { throwable ->
            Log.w("SOSPatientViewModel", "No se pudieron iniciar las actualizaciones de ubicación.", throwable)
        }
    }

    private fun updateLocation(lat: Double, lng: Double) {
        createOrUpdateAlert(latitude = lat, longitude = lng)
    }

    private var pendingLatitude: Double? = null
    private var pendingLongitude: Double? = null

    private fun createOrUpdateAlert(latitude: Double?, longitude: Double?) {
        val alertId = currentAlertId
        if (alertId == null) {
            if (isCreatingAlert) {
                if (latitude != null && longitude != null) {
                    pendingLatitude = latitude
                    pendingLongitude = longitude
                }
                return
            }
            isCreatingAlert = true
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
                latitude = latitude,
                longitude = longitude,
                createdAtText = dateFormat.format(Date())
            )

            viewModelScope.launch {
                val result = firestoreRepository.createAlert(newAlert)
                if (result.isSuccess) {
                    val createdAlertId = result.getOrNull()
                    currentAlertId = createdAlertId
                    if (createdAlertId != null) {
                        val linkedCaregiverId = state.caregiverId
                        if (linkedCaregiverId != null) {
                            notificationApiService.sendSosAlert(
                                patientId = state.patientId,
                                caregiverId = linkedCaregiverId,
                                alertId = createdAlertId,
                                patientName = state.patientName
                            ).onFailure { throwable ->
                                Log.w(
                                    "SOSPatientViewModel",
                                    "La alerta se guardó, pero no se pudo enviar la notificación push.",
                                    throwable
                                )
                            }
                        }
                    }
                    if (pendingLatitude != null && pendingLongitude != null) {
                        createOrUpdateAlert(pendingLatitude, pendingLongitude)
                        pendingLatitude = null
                        pendingLongitude = null
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSOSActive = false,
                        error = result.exceptionOrNull().toUserFriendlyMessage("No se pudo enviar la alerta SOS.")
                    )
                }
                isCreatingAlert = false
            }
        } else {
            if (latitude != null && longitude != null) {
                viewModelScope.launch {
                    firestoreRepository.updateAlertLocation(alertId, latitude, longitude)
                }
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

    fun updateNotificationsEnabled(enabled: Boolean) {
        val patientId = _uiState.value.patientId.ifBlank { authService.getCurrentUser()?.uid.orEmpty() }
        if (patientId.isBlank()) return

        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        viewModelScope.launch {
            firestoreRepository.updatePatient(patientId, mapOf("notificationsEnabled" to enabled))
        }
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
    val caregiverName: String = "",
    val notificationsEnabled: Boolean = true,
    val error: String? = null
)

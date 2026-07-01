package com.relaxmind.app.features.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.relaxmind.app.data.model.CaregiverAlert
import com.relaxmind.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SOSAlertViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SOSAlertUiState())
    val uiState: StateFlow<SOSAlertUiState> = _uiState.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null
    private var currentAlertId: String? = null

    fun loadAlert(alertId: String) {
        if (currentAlertId == alertId) return
        currentAlertId = alertId
        
        listenerRegistration?.remove()
        
        listenerRegistration = firestoreRepository.listenToAlert(
            alertId = alertId,
            onChange = { alert ->
                if (alert != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        alert = alert,
                        error = null
                    )
                    loadPatientData(alert.patientId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Alerta no encontrada o eliminada"
                    )
                }
            },
            onError = { error ->
                android.util.Log.e("SOSAlertViewModel", "Error al escuchar la alerta $alertId", error)
                val msg = if (error.message?.contains("PERMISSION_DENIED") == true) {
                    "No tienes permisos para ver esta alerta o la alerta no existe."
                } else {
                    error.message ?: "Error desconocido"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = msg
                )
            }
        )
    }

    private fun loadPatientData(patientId: String) {
        if (_uiState.value.patientAvatarUrl.isNotEmpty()) return

        viewModelScope.launch {
            val patientResult = firestoreRepository.getPatientById(patientId)
            val patient = patientResult.getOrNull()
            if (patient != null) {
                _uiState.value = _uiState.value.copy(
                    patientAvatarUrl = patient.avatarUrl,
                    patientPhone = patient.phone
                )
            }
        }
    }

    fun markResolved(onSuccess: () -> Unit) {
        val alertId = currentAlertId ?: return
        viewModelScope.launch {
            val result = firestoreRepository.updateAlertFields(
                alertId,
                mapOf(
                    "resolved" to true,
                    "title" to "Alerta SOS resuelta",
                    "message" to "El cuidador marcó la emergencia como resuelta.",
                    "severity" to "resolved"
                )
            )
            if (result.isSuccess) {
                onSuccess()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}

data class SOSAlertUiState(
    val isLoading: Boolean = true,
    val alert: CaregiverAlert? = null,
    val error: String? = null,
    val patientAvatarUrl: String = "",
    val patientPhone: String = ""
)

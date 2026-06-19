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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Error desconocido"
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
            val result = firestoreRepository.updateAlertResolved(alertId, true)
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

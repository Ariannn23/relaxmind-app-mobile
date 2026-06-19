package com.relaxmind.app.features.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.relaxmind.app.data.model.Caregiver
import com.relaxmind.app.data.model.CaregiverAlert
import com.relaxmind.app.data.model.Patient
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CaregiverPatientSummary(
    val patient: Patient,
    val latestScore: Int? = null
)

class CaregiverViewModel(
    private val authService: FirebaseAuthService = FirebaseAuthService(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {
    private val _caregiver = MutableStateFlow<Caregiver?>(null)
    val caregiver = _caregiver.asStateFlow()

    private val _patients = MutableStateFlow<List<CaregiverPatientSummary>>(emptyList())
    val patients = _patients.asStateFlow()

    private val _activeAlerts = MutableStateFlow<List<CaregiverAlert>>(emptyList())
    val activeAlerts = _activeAlerts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isLinking = MutableStateFlow(false)
    val isLinking = _isLinking.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadDashboard() {
        val caregiverId = authService.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            firestoreRepository.getCaregiverById(caregiverId)
                .onSuccess { _caregiver.value = it }
                .onFailure { _error.value = it.localizedMessage ?: "No se pudo cargar el cuidador." }

            firestoreRepository.getPatientsForCaregiver(caregiverId)
                .onSuccess { patientList ->
                    _patients.value = patientList.map { patient ->
                        val score = firestoreRepository.getLatestCheckIn(patient.id).getOrNull()?.score
                        CaregiverPatientSummary(patient = patient, latestScore = score)
                    }
                }
                .onFailure { _error.value = it.localizedMessage ?: "No se pudieron cargar los pacientes." }

            firestoreRepository.getActiveCaregiverAlerts(caregiverId)
                .onSuccess { _activeAlerts.value = it }
                .onFailure { _error.value = it.localizedMessage ?: "No se pudieron cargar las alertas." }

            _isLoading.value = false
        }
    }

    fun verifyBindingCode(code: String, onSuccess: () -> Unit) {
        val caregiverId = authService.getCurrentUser()?.uid
        if (caregiverId.isNullOrBlank()) {
            _error.value = "No se encontró una sesión activa."
            return
        }

        val sanitizedCode = code.filter { it.isDigit() }
        if (sanitizedCode.length != 6) {
            _error.value = "Ingresa un código de 6 dígitos."
            return
        }

        viewModelScope.launch {
            _isLinking.value = true
            _error.value = null

            firestoreRepository.linkPatientWithCode(sanitizedCode, caregiverId)
                .onSuccess {
                    _message.value = "Vinculación exitosa"
                    loadDashboard()
                    onSuccess()
                }
                .onFailure { throwable ->
                    _error.value = throwable.localizedMessage ?: "Código inválido o expirado"
                }

            _isLinking.value = false
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun consumeError() {
        _error.value = null
    }
}

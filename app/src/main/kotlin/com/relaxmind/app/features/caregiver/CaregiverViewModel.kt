package com.relaxmind.app.features.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.relaxmind.app.data.model.Caregiver
import com.relaxmind.app.data.model.CaregiverAlert
import com.relaxmind.app.data.model.CheckIn
import com.relaxmind.app.data.model.Patient
import com.relaxmind.app.data.model.Streak
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.relaxmind.app.utils.toUserFriendlyMessage

data class CaregiverPatientSummary(
    val patient: Patient,
    val latestCheckIn: CheckIn? = null,
    val hasPendingAlert: Boolean = false
) {
    val latestScore: Int? = latestCheckIn?.score
    val lastCheckInDate: String? = latestCheckIn?.date
}

data class PendingPatientLink(
    val code: String,
    val patient: Patient
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

    private val _allAlerts = MutableStateFlow<List<CaregiverAlert>>(emptyList())
    val allAlerts = _allAlerts.asStateFlow()

    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    val selectedPatient = _selectedPatient.asStateFlow()

    private val _selectedPatientCheckIns = MutableStateFlow<List<CheckIn>>(emptyList())
    val selectedPatientCheckIns = _selectedPatientCheckIns.asStateFlow()

    private val _selectedPatientStreak = MutableStateFlow<Streak?>(null)
    val selectedPatientStreak = _selectedPatientStreak.asStateFlow()

    private val _selectedPatientAlerts = MutableStateFlow<List<CaregiverAlert>>(emptyList())
    val selectedPatientAlerts = _selectedPatientAlerts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isPatientsLoading = MutableStateFlow(true)
    val isPatientsLoading = _isPatientsLoading.asStateFlow()

    private val _isLinking = MutableStateFlow(false)
    val isLinking = _isLinking.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _linkedPatientName = MutableStateFlow<String?>(null)
    val linkedPatientName = _linkedPatientName.asStateFlow()

    private val _pendingPatientLink = MutableStateFlow<PendingPatientLink?>(null)
    val pendingPatientLink = _pendingPatientLink.asStateFlow()

    private var patientsListener: ListenerRegistration? = null
    private var alertsListener: ListenerRegistration? = null
    private var patientAlertsListener: ListenerRegistration? = null
    private var rawPatients: List<Patient> = emptyList()

    fun loadDashboard() {
        observeCaregiverData()
    }

    fun observeCaregiverData() {
        val caregiverId = authService.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            firestoreRepository.getCaregiverById(caregiverId)
                .onSuccess { _caregiver.value = it }
                .onFailure { _error.value = it.toUserFriendlyMessage("No se pudo cargar el cuidador.") }

            _isLoading.value = false
        }

        if (patientsListener != null && alertsListener != null) return

        _isPatientsLoading.value = true

        patientsListener = firestoreRepository.listenPatientsForCaregiver(
            caregiverId = caregiverId,
            onChange = { patients ->
                rawPatients = patients
                rebuildPatientSummaries()
            },
            onError = {
                _isPatientsLoading.value = false
                _error.value = it.toUserFriendlyMessage("No se pudieron escuchar los pacientes.")
            }
        )

        alertsListener = firestoreRepository.listenAlertsForCaregiver(
            caregiverId = caregiverId,
            onChange = { alerts ->
                _allAlerts.value = alerts
                _activeAlerts.value = alerts.filter { !it.resolved }
                rebuildPatientSummaries()
            },
            onError = { _error.value = it.toUserFriendlyMessage("No se pudieron escuchar las alertas.") }
        )
    }

    fun loadPatientDetail(patientId: String) {
        val caregiverId = authService.getCurrentUser()?.uid ?: return
        observeCaregiverData()

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            firestoreRepository.getPatientById(patientId)
                .onSuccess { _selectedPatient.value = it }
                .onFailure { _error.value = it.toUserFriendlyMessage("No se pudo cargar el paciente.") }

            firestoreRepository.getPatientCheckIns(patientId)
                .onSuccess { _selectedPatientCheckIns.value = it.sortedByDescending { checkIn -> checkIn.date } }
                .onFailure { _error.value = it.toUserFriendlyMessage("No se pudo cargar el historial.") }

            firestoreRepository.getPatientStreak(patientId)
                .onSuccess { _selectedPatientStreak.value = it }
                .onFailure { _error.value = it.toUserFriendlyMessage("No se pudo cargar la racha.") }

            _isLoading.value = false
        }

        patientAlertsListener?.remove()
        patientAlertsListener = firestoreRepository.listenAlertsForPatient(
            caregiverId = caregiverId,
            patientId = patientId,
            onChange = { _selectedPatientAlerts.value = it },
            onError = { _error.value = it.toUserFriendlyMessage("No se pudieron escuchar las alertas del paciente.") }
        )
    }

    fun markAlertResolved(alertId: String) {
        viewModelScope.launch {
            firestoreRepository.updateAlertResolved(alertId, true)
                .onSuccess { _message.value = "Alerta marcada como resuelta" }
                .onFailure { _error.value = it.toUserFriendlyMessage("No se pudo resolver la alerta.") }
        }
    }

    fun previewBindingCode(code: String) {
        val caregiverId = authService.getCurrentUser()?.uid
        if (caregiverId.isNullOrBlank()) {
            _error.value = "No se encontro una sesion activa."
            return
        }

        val sanitizedCode = code.filter { it.isDigit() }
        if (sanitizedCode.length != 6) {
            _error.value = "Ingresa un codigo de 6 digitos."
            return
        }

        viewModelScope.launch {
            _isLinking.value = true
            _error.value = null

            firestoreRepository.previewPatientForBindingCode(sanitizedCode, caregiverId)
                .onSuccess { patient ->
                    _pendingPatientLink.value = PendingPatientLink(
                        code = sanitizedCode,
                        patient = patient
                    )
                }
                .onFailure { throwable ->
                    _error.value = throwable.toUserFriendlyMessage("Codigo invalido o expirado")
                }

            _isLinking.value = false
        }
    }

    fun confirmPendingPatientLink(onSuccess: () -> Unit) {
        val caregiverId = authService.getCurrentUser()?.uid
        if (caregiverId.isNullOrBlank()) {
            _error.value = "No se encontro una sesion activa."
            return
        }

        val pending = _pendingPatientLink.value ?: run {
            _error.value = "Primero verifica un codigo de vinculacion."
            return
        }

        viewModelScope.launch {
            _isLinking.value = true
            _error.value = null

            firestoreRepository.linkPatientWithCode(pending.code, caregiverId)
                .onSuccess { patientId ->
                    firestoreRepository.getPatientById(patientId).onSuccess { p ->
                        _linkedPatientName.value = "${p?.name.orEmpty()} ${p?.lastName.orEmpty()}".trim().ifBlank { "Paciente" }
                    }
                    _pendingPatientLink.value = null
                    _message.value = "Vinculacion exitosa"
                    loadDashboard()
                    onSuccess()
                }
                .onFailure { throwable ->
                    _error.value = throwable.toUserFriendlyMessage("No se pudo vincular al paciente")
                }

            _isLinking.value = false
        }
    }

    fun clearPendingPatientLink() {
        _pendingPatientLink.value = null
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun consumeError() {
        _error.value = null
    }

    private fun rebuildPatientSummaries() {
        viewModelScope.launch {
            val pendingPatientIds = _allAlerts.value
                .filter { !it.resolved }
                .map { it.patientId }
                .toSet()

            _patients.value = rawPatients.map { patient ->
                CaregiverPatientSummary(
                    patient = patient,
                    latestCheckIn = firestoreRepository.getLatestCheckIn(patient.id).getOrNull(),
                    hasPendingAlert = pendingPatientIds.contains(patient.id)
                )
            }.sortedBy { it.patient.name }
            
            _isPatientsLoading.value = false
        }
    }

    fun updateProfile(
        name: String,
        lastName: String,
        birthDate: String,
        sex: String,
        phone: String,
        avatarUrl: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = authService.getCurrentUser()?.uid
                if (uid != null) {
                    val current = _caregiver.value
                    if (current == null) {
                        onError("No se pudieron cargar tus datos para editar.")
                        return@launch
                    }
                    val updated = current.copy(
                        name = name,
                        lastName = lastName,
                        birthDate = birthDate,
                        sex = sex,
                        phone = phone,
                        avatarUrl = avatarUrl.ifBlank { current.avatarUrl }
                    )
                    firestoreRepository.updateCaregiver(
                        uid,
                        mapOf(
                            "name" to updated.name,
                            "lastName" to updated.lastName,
                            "birthDate" to updated.birthDate,
                            "sex" to updated.sex,
                            "phone" to updated.phone,
                            "avatarUrl" to updated.avatarUrl
                        )
                    )
                        .onSuccess {
                            _caregiver.value = updated
                            onSuccess()
                        }
                        .onFailure { error ->
                            onError(error.toUserFriendlyMessage("Error al guardar el perfil."))
                        }
                } else {
                    onError("No se encontró sesión activa.")
                }
            } catch (e: Exception) {
                onError(e.toUserFriendlyMessage("Error inesperado."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        patientsListener?.remove()
        alertsListener?.remove()
        patientAlertsListener?.remove()
        super.onCleared()
    }
    fun updateDarkMode(enabled: Boolean) {
        // Stub
    }

    fun updateLanguage(lang: String) {
        val uid = authService.getCurrentUser()?.uid ?: return
        com.relaxmind.app.ui.themes.ThemeState.language.value = lang
        _caregiver.value = _caregiver.value?.copy(language = lang)

        viewModelScope.launch {
            firestoreRepository.updateCaregiver(uid, mapOf("language" to lang))
                .onFailure { error ->
                    _error.value = error.toUserFriendlyMessage("No se pudo actualizar el idioma.")
                }
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        val uid = authService.getCurrentUser()?.uid ?: return
        _caregiver.value = _caregiver.value?.copy(notificationsEnabled = enabled)
        viewModelScope.launch {
            firestoreRepository.updateCaregiver(uid, mapOf("notificationsEnabled" to enabled))
                .onFailure { _error.value = it.toUserFriendlyMessage("No se pudo guardar la preferencia.") }
        }
    }

    fun updateBiometricEnabled(enabled: Boolean) {
        val uid = authService.getCurrentUser()?.uid ?: return
        _caregiver.value = _caregiver.value?.copy(biometricEnabled = enabled)
        viewModelScope.launch {
            firestoreRepository.updateCaregiver(uid, mapOf("biometricEnabled" to enabled))
                .onFailure { _error.value = it.toUserFriendlyMessage("No se pudo guardar la preferencia.") }
        }
    }

    fun deleteAccount(reason: String, passwordConfirm: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Stub
        onSuccess()
    }

    fun logout() {
        authService.logout()
    }
}



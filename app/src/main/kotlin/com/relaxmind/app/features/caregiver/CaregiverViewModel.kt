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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.relaxmind.app.utils.toUserFriendlyMessage
import java.time.LocalDate

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
    val isPatientsLoading: StateFlow<Boolean> = _isPatientsLoading.asStateFlow()

    private val _isAlertsLoading = MutableStateFlow(true)
    val isAlertsLoading: StateFlow<Boolean> = _isAlertsLoading.asStateFlow()

    private val _isLinking = MutableStateFlow(false)
    val isLinking: StateFlow<Boolean> = _isLinking.asStateFlow()

    private val _isPatientDetailLoading = MutableStateFlow(false)
    val isPatientDetailLoading: StateFlow<Boolean> = _isPatientDetailLoading.asStateFlow()

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
    private var observedCaregiverId: String? = null

    fun loadDashboard() {
        observeCaregiverData()
    }

    fun observeCaregiverData() {
        val caregiverId = authService.getCurrentUser()?.uid ?: run {
            clearCaregiverSessionState()
            return
        }

        if (observedCaregiverId != null && observedCaregiverId != caregiverId) {
            clearCaregiverSessionState()
        }
        observedCaregiverId = caregiverId

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            firestoreRepository.getCaregiverById(caregiverId)
                .onSuccess {
                    if (isCurrentCaregiver(caregiverId)) {
                        _caregiver.value = it
                    }
                }
                .onFailure {
                    if (isCurrentCaregiver(caregiverId)) {
                        _error.value = it.toUserFriendlyMessage("No se pudo cargar el cuidador.")
                    }
                }

            if (isCurrentCaregiver(caregiverId)) {
                _isLoading.value = false
            }
        }

        if (patientsListener != null && alertsListener != null) return

        removeRealtimeListeners()

        _isPatientsLoading.value = true
        _isAlertsLoading.value = true

        patientsListener = firestoreRepository.listenPatientsForCaregiver(
            caregiverId = caregiverId,
            onChange = { patients ->
                if (!isCurrentCaregiver(caregiverId)) return@listenPatientsForCaregiver
                rawPatients = patients
                rebuildPatientSummaries(caregiverId)
            },
            onError = {
                if (!isCurrentCaregiver(caregiverId)) return@listenPatientsForCaregiver
                _isPatientsLoading.value = false
                _error.value = it.toUserFriendlyMessage("No se pudieron escuchar los pacientes.")
            }
        )

        alertsListener = firestoreRepository.listenAlertsForCaregiver(
            caregiverId = caregiverId,
            onChange = { alerts ->
                if (!isCurrentCaregiver(caregiverId)) return@listenAlertsForCaregiver
                _allAlerts.value = alerts
                _activeAlerts.value = alerts.filter { !it.resolved }
                _isAlertsLoading.value = false
                rebuildPatientSummaries(caregiverId)
            },
            onError = {
                if (!isCurrentCaregiver(caregiverId)) return@listenAlertsForCaregiver
                _isAlertsLoading.value = false
                _error.value = it.toUserFriendlyMessage("No se pudieron escuchar las alertas.")
            }
        )
    }

    fun loadPatientDetail(patientId: String) {
        val caregiverId = authService.getCurrentUser()?.uid ?: return
        observeCaregiverData()

        val cachedPatient = rawPatients.firstOrNull { it.id == patientId }
            ?: _patients.value.firstOrNull { it.patient.id == patientId }?.patient
        _selectedPatient.value = cachedPatient
        _selectedPatientCheckIns.value = emptyList()
        _selectedPatientStreak.value = null
        _selectedPatientAlerts.value = emptyList()
        _isPatientDetailLoading.value = true

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            firestoreRepository.getPatientById(patientId)
                .onSuccess { patient ->
                    if (!isCurrentCaregiver(caregiverId)) return@onSuccess
                    if (patient != null) {
                        _selectedPatient.value = patient
                    } else if (cachedPatient == null) {
                        _error.value = "No se pudo cargar el paciente."
                    }
                }
                .onFailure {
                    if (!isCurrentCaregiver(caregiverId)) return@onFailure
                    if (cachedPatient == null) {
                        _error.value = it.toUserFriendlyMessage("No se pudo cargar el paciente.")
                    }
                }

            firestoreRepository.getPatientCheckIns(patientId)
                .onSuccess {
                    if (isCurrentCaregiver(caregiverId)) {
                        _selectedPatientCheckIns.value = it.sortedByDescending { checkIn -> checkIn.date }
                    }
                }
                .onFailure {
                    if (isCurrentCaregiver(caregiverId)) {
                        _selectedPatientCheckIns.value = emptyList()
                    }
                }

            firestoreRepository.getPatientStreak(patientId)
                .onSuccess {
                    if (isCurrentCaregiver(caregiverId)) {
                        _selectedPatientStreak.value = it
                    }
                }
                .onFailure {
                    if (isCurrentCaregiver(caregiverId)) {
                        _selectedPatientStreak.value = null
                    }
                }

            if (isCurrentCaregiver(caregiverId)) {
                _isLoading.value = false
                _isPatientDetailLoading.value = false
            }
        }

        patientAlertsListener?.remove()
        patientAlertsListener = firestoreRepository.listenAlertsForPatient(
            caregiverId = caregiverId,
            patientId = patientId,
            onChange = {
                if (isCurrentCaregiver(caregiverId)) {
                    _selectedPatientAlerts.value = it
                }
            },
            onError = {
                if (isCurrentCaregiver(caregiverId)) {
                    _selectedPatientAlerts.value = emptyList()
                }
            }
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
                    observeCaregiverData()
                    onSuccess()
                }
                .onFailure { throwable ->
                    _error.value = throwable.toUserFriendlyMessage("No se pudo vincular al paciente")
                }

            _isLinking.value = false
        }
    }

    fun unlinkPatient(patientId: String, passwordConfirm: String, reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _error.value = null

            val reauthResult = authService.reauthenticate(passwordConfirm)
            if (reauthResult.isFailure) {
                val errorMsg = reauthResult.exceptionOrNull().toUserFriendlyMessage("Contraseña incorrecta.")
                onError(errorMsg)
                return@launch
            }

            val updateResult = firestoreRepository.updatePatient(
                patientId,
                mapOf<String, Any?>(
                    "caregiverId" to null,
                    "linkedCaregiverAt" to null,
                    "caregiverName" to null,
                    "caregiverLastName" to null,
                    "caregiverEmail" to null,
                    "caregiverPhone" to null,
                    "caregiverAvatarUrl" to null,
                    "pendingCaregiverUnlinkAlert" to true
                )
            )

            if (updateResult.isSuccess) {
                observeCaregiverData()
                onSuccess()
            } else {
                val errorMsg = updateResult.exceptionOrNull().toUserFriendlyMessage("Error al desvincular.")
                onError(errorMsg)
            }
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

    private fun rebuildPatientSummaries(ownerCaregiverId: String) {
        viewModelScope.launch {
            val sourcePatients = rawPatients
            val sourceAlerts = _allAlerts.value
            val pendingPatientIds = sourceAlerts
                .filter { !it.resolved }
                .map { it.patientId }
                .toSet()

            val summaries = sourcePatients.map { patient ->
                CaregiverPatientSummary(
                    patient = patient,
                    latestCheckIn = firestoreRepository.getLatestCheckIn(patient.id).getOrNull(),
                    hasPendingAlert = pendingPatientIds.contains(patient.id)
                )
            }.sortedBy { it.patient.name }

            if (isCurrentCaregiver(ownerCaregiverId) && sourceAlerts == _allAlerts.value) {
                _patients.value = summaries
                _isPatientsLoading.value = false
            }
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
        removeRealtimeListeners()
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
        val userId = authService.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val reauthResult = authService.reauthenticate(passwordConfirm)
            if (reauthResult.isFailure) {
                val errorMsg = reauthResult.exceptionOrNull().toUserFriendlyMessage("Contraseña incorrecta.")
                onError(errorMsg)
                _isLoading.value = false
                return@launch
            }

            val todayDate = LocalDate.now().toString()
            val updateResult = firestoreRepository.updateCaregiver(
                userId,
                mapOf(
                    "isDeleted" to true,
                    "deletedAt" to todayDate,
                    "deletionReason" to reason
                )
            )

            if (updateResult.isSuccess) {
                clearCaregiverSessionState()
                authService.logout()
                onSuccess()
            } else {
                val errorMsg = updateResult.exceptionOrNull().toUserFriendlyMessage("Error al borrar la cuenta.")
                onError(errorMsg)
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        clearCaregiverSessionState()
        authService.logout()
    }

    private fun isCurrentCaregiver(caregiverId: String): Boolean {
        return observedCaregiverId == caregiverId && authService.getCurrentUser()?.uid == caregiverId
    }

    private fun removeRealtimeListeners() {
        patientsListener?.remove()
        alertsListener?.remove()
        patientAlertsListener?.remove()
        patientsListener = null
        alertsListener = null
        patientAlertsListener = null
    }

    private fun clearCaregiverSessionState() {
        removeRealtimeListeners()
        observedCaregiverId = null
        rawPatients = emptyList()
        _caregiver.value = null
        _patients.value = emptyList()
        _activeAlerts.value = emptyList()
        _allAlerts.value = emptyList()
        _selectedPatient.value = null
        _selectedPatientCheckIns.value = emptyList()
        _selectedPatientStreak.value = null
        _selectedPatientAlerts.value = emptyList()
        _pendingPatientLink.value = null
        _linkedPatientName.value = null
        _isLoading.value = false
        _isPatientsLoading.value = false
        _isAlertsLoading.value = false
        _isLinking.value = false
        _isPatientDetailLoading.value = false
        _message.value = null
        _error.value = null
    }
}



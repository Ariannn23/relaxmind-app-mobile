package com.relaxmind.app.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.relaxmind.app.data.model.Caregiver
import com.relaxmind.app.data.model.Patient
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import com.relaxmind.app.utils.ValidationUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

class AuthViewModel(
    private val authServiceFactory: () -> FirebaseAuthService = { FirebaseAuthService() },
    private val firestoreRepositoryFactory: () -> FirestoreRepository = { FirestoreRepository() }
) : ViewModel() {
    private val authService: FirebaseAuthService by lazy(authServiceFactory)
    private val firestoreRepository: FirestoreRepository by lazy(firestoreRepositoryFactory)

    // ── Public StateFlows ──────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** "patient", "caregiver", or null when unknown / logged-out. */
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    /** How many times the user has requested a code resend (max 5). */
    private val _resendCount = MutableStateFlow(0)
    val resendCount: StateFlow<Int> = _resendCount.asStateFlow()

    /** Countdown in seconds (120 → 0). Active while a code is pending. */
    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    // ── Private helpers ────────────────────────────────────────────────────

    /** In-memory OTP used for simulation (Firebase sends email links; here we simulate a 6-digit code). */
    private var pendingOtp: String = ""

    private var timerJob: Job? = null

    private companion object {
        const val OTP_LENGTH = 6
        const val RESEND_MAX = 5
        const val TIMER_SECONDS = 120
    }

    // ── Auth functions ─────────────────────────────────────────────────────

    /**
     * Validates all registration fields, creates a Firebase Auth account,
     * stores the user profile in Firestore, and emits success.
     */
    fun register(
        name: String,
        lastName: String,
        birthDate: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String,
        phone: String
    ) {
        // Field validation
        val validationError =
            ValidationUtils.validateName(name)
                ?: ValidationUtils.validateLastName(lastName)
                ?: ValidationUtils.validateBirthDate(birthDate)
                ?: ValidationUtils.validateEmail(email)
                ?: ValidationUtils.validatePassword(password)
                ?: ValidationUtils.validateConfirmPassword(password, confirmPassword)
                ?: ValidationUtils.validatePhone(phone)
                ?: ValidationUtils.validateRole(role)

        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = false) }

            // 1. Create Firebase Auth account
            val registerResult = authService.register(email, password)
            if (registerResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = registerResult.exceptionOrNull().toUserMessage(
                            fallback = "Error al crear la cuenta."
                        )
                    )
                }
                return@launch
            }

            val firebaseUser = registerResult.getOrNull()!!
            val userId = firebaseUser.uid
            val createdAt = Instant.now().toString()

            // 2. Persist profile in Firestore according to role
            val firestoreResult = when (role) {
                "patient" -> firestoreRepository.createPatient(
                    Patient(
                        id = userId,
                        name = name,
                        lastName = lastName,
                        birthDate = birthDate,
                        email = email,
                        phone = phone,
                        createdAt = createdAt
                    )
                )
                "caregiver" -> firestoreRepository.createCaregiver(
                    Caregiver(
                        id = userId,
                        name = name,
                        lastName = lastName,
                        birthDate = birthDate,
                        email = email,
                        phone = phone,
                        createdAt = createdAt
                    )
                )
                else -> Result.failure(IllegalArgumentException("Rol desconocido: $role"))
            }

            if (firestoreResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = firestoreResult.exceptionOrNull()?.localizedMessage
                            ?: "Error al guardar el perfil."
                    )
                }
                return@launch
            }

            // 3. Send verification e-mail (non-blocking; ignore failure to keep UX smooth)
            authService.sendVerificationEmail()

            _userRole.value = role
            _uiState.update { it.copy(isLoading = false, success = true) }
        }
    }

    /**
     * Signs the user in and resolves their role from Firestore so the UI
     * can navigate to the correct dashboard.
     */
    fun login(email: String, password: String) {
        val emailError = ValidationUtils.validateEmail(email)
        if (emailError != null) {
            _uiState.update { it.copy(error = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = false) }

            val loginResult = authService.login(email, password)
            if (loginResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = loginResult.exceptionOrNull()?.localizedMessage
                            ?: "Correo o contraseña incorrectos."
                    )
                }
                return@launch
            }

            val userId = loginResult.getOrNull()!!.uid
            val roleResult = firestoreRepository.getRoleById(userId)

            if (roleResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = roleResult.exceptionOrNull()?.localizedMessage
                            ?: "No se pudo determinar el rol del usuario."
                    )
                }
                return@launch
            }

            _userRole.value = roleResult.getOrNull()
            _uiState.update { it.copy(isLoading = false, success = true) }
        }
    }

    // ── OTP / Verification ─────────────────────────────────────────────────

    /**
     * Simulates sending a 6-digit OTP. In production this would be replaced
     * by a real SMS/OTP backend call; Firebase itself uses email deep-links.
     *
     * The function generates a random code, starts the 120-second countdown,
     * and (in debug builds) logs the code so it can be tested manually.
     */
    fun sendVerificationCode() {
        pendingOtp = (100_000..999_999).random().toString()
        Log.d("RelaxMindOtp", "Verification OTP: $pendingOtp")
        _resendCount.value = 0
        startTimer()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Reuse Firebase email verification as the real delivery mechanism.
            val result = authService.sendVerificationEmail()

            _uiState.update {
                if (result.isFailure) {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.localizedMessage
                            ?: "Error al enviar el código."
                    )
                } else {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    /** Validates the 6-digit OTP entered by the user. */
    fun verifyCode(code: String) {
        val codeError = ValidationUtils.validateOtpCode(code)
        if (codeError != null) {
            _uiState.update { it.copy(error = codeError) }
            return
        }

        if (code != pendingOtp) {
            _uiState.update { it.copy(error = "El código ingresado no es correcto.") }
            return
        }

        // Code is valid
        timerJob?.cancel()
        _timerSeconds.value = 0
        _uiState.update { it.copy(success = true, error = null) }
    }

    /**
     * Resends the verification code up to [RESEND_MAX] times.
     * Resets the countdown on each successful resend.
     */
    fun resendCode() {
        if (_resendCount.value >= RESEND_MAX) {
            _uiState.update {
                it.copy(error = "Has alcanzado el límite máximo de reenvíos ($RESEND_MAX).")
            }
            return
        }

        _resendCount.update { it + 1 }
        pendingOtp = (100_000..999_999).random().toString()
        Log.d("RelaxMindOtp", "Verification OTP: $pendingOtp")
        startTimer()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authService.sendVerificationEmail()
            _uiState.update {
                if (result.isFailure) {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.localizedMessage
                            ?: "Error al reenviar el código."
                    )
                } else {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    // ── Profile helpers ────────────────────────────────────────────────────

    /** Persists the chosen avatar URL for the currently authenticated user. */
    fun updateAvatar(avatarUrl: String) {
        val userId = authService.getCurrentUser()?.uid ?: run {
            _uiState.update { it.copy(error = "No hay sesión activa.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val role = resolveCurrentRole(userId).getOrNull()
            val result = when (role) {
                "patient" -> firestoreRepository.updatePatient(userId, mapOf("avatarUrl" to avatarUrl))
                "caregiver" -> firestoreRepository.updateCaregiver(userId, mapOf("avatarUrl" to avatarUrl))
                else -> Result.failure(IllegalStateException("Rol desconocido: $role"))
            }

            _uiState.update {
                if (result.isFailure) {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.localizedMessage
                            ?: "Error al actualizar el avatar."
                    )
                } else {
                    it.copy(isLoading = false, success = true)
                }
            }
        }
    }

    /** Persists the notification preference for the currently authenticated user. */
    fun setNotificationPermission(enabled: Boolean) {
        val userId = authService.getCurrentUser()?.uid ?: run {
            _uiState.update { it.copy(error = "No hay sesión activa.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val role = resolveCurrentRole(userId).getOrNull()
            val result = when (role) {
                "patient" -> firestoreRepository.updatePatient(
                    userId, mapOf("notificationsEnabled" to enabled)
                )
                "caregiver" -> firestoreRepository.updateCaregiver(
                    userId, mapOf("notificationsEnabled" to enabled)
                )
                else -> Result.failure(IllegalStateException("Rol desconocido: $role"))
            }

            _uiState.update {
                if (result.isFailure) {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.localizedMessage
                            ?: "Error al actualizar las notificaciones."
                    )
                } else {
                    it.copy(isLoading = false, success = true)
                }
            }
        }
    }

    /** Sends a password-reset e-mail via Firebase Auth. */
    fun resetPassword(email: String) {
        val emailError = ValidationUtils.validateEmail(email)
        if (emailError != null) {
            _uiState.update { it.copy(error = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = false) }

            val result = authService.resetPassword(email)
            _uiState.update {
                if (result.isFailure) {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.localizedMessage
                            ?: "Error al enviar el correo de recuperación."
                    )
                } else {
                    it.copy(isLoading = false, success = true)
                }
            }
        }
    }

    /** Clears any active error from the UI state. */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(success = false) }
    }

    private suspend fun resolveCurrentRole(userId: String): Result<String> {
        _userRole.value?.let { return Result.success(it) }

        val roleResult = firestoreRepository.getRoleById(userId)
        roleResult.getOrNull()?.let { role -> _userRole.value = role }
        return roleResult
    }

    // ── Timer ──────────────────────────────────────────────────────────────

    private fun startTimer() {
        timerJob?.cancel()
        _timerSeconds.value = TIMER_SECONDS

        timerJob = viewModelScope.launch {
            while (_timerSeconds.value > 0) {
                delay(1_000L)
                _timerSeconds.update { it - 1 }
            }
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

private fun Throwable?.toUserMessage(fallback: String): String {
    val message = this?.localizedMessage.orEmpty()
    return when {
        "CONFIGURATION_NOT_FOUND" in message ->
            "Firebase Auth no está configurado. Activa Email/Password en Firebase Console."
        "email address is badly formatted" in message ->
            "El correo electrónico no tiene un formato válido."
        message.isNotBlank() -> message
        else -> fallback
    }
}

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
import kotlinx.coroutines.tasks.await
import java.time.Instant

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
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
     * Finishes the registration of a new Google user by saving their chosen role.
     */
    fun finishGoogleRegistration(role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = false) }

            val firebaseUser = authService.getCurrentUser()
            if (firebaseUser == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado.") }
                return@launch
            }

            val userId = firebaseUser.uid
            val nameParts = firebaseUser.displayName?.split(" ") ?: listOf("Usuario")
            val name = nameParts.firstOrNull() ?: "Usuario"
            val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""
            val email = firebaseUser.email ?: ""
            val createdAt = Instant.now().toString()

            val firestoreResult = when (role) {
                "patient" -> firestoreRepository.createPatient(
                    Patient(
                        id = userId,
                        name = name,
                        lastName = lastName,
                        birthDate = "",
                        email = email,
                        createdAt = createdAt
                    )
                )
                "caregiver" -> firestoreRepository.createCaregiver(
                    Caregiver(
                        id = userId,
                        name = name,
                        lastName = lastName,
                        birthDate = "",
                        email = email,
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

    /**
     * Signs the user in using Google credentials.
     * If the user doesn't have a role yet (new account), success is true but userRole is null.
     */
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = false) }

            val loginResult = authService.loginWithGoogleCredential(idToken)
            if (loginResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = loginResult.exceptionOrNull()?.localizedMessage
                            ?: "Error al iniciar sesión con Google."
                    )
                }
                return@launch
            }

            val userId = loginResult.getOrNull()!!.uid
            val roleResult = firestoreRepository.getRoleById(userId)

            // If it's a new Google user, roleResult will fail/be null, which is expected
            _userRole.value = roleResult.getOrNull()
            _uiState.update { it.copy(isLoading = false, success = true) }
        }
    }

    // ── OTP / Verification ─────────────────────────────────────────────────

    fun sendVerificationLink() {
        viewModelScope.launch {
            val result = authService.sendVerificationEmail()
            if (result.isFailure) {
                _uiState.update {
                    it.copy(
                        error = result.exceptionOrNull()?.localizedMessage
                            ?: "Error al enviar enlace de verificación."
                    )
                }
            }
        }
    }

    /**
     * Checks if the user's email is verified.
     */
    fun checkEmailVerified() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = authService.getCurrentUser()
            if (user == null) {
                _uiState.update { it.copy(isLoading = false, error = "No hay usuario en sesión.") }
                return@launch
            }
            
            try {
                user.reload().await()
                if (user.isEmailVerified) {
                    val role = resolveCurrentRole(user.uid).getOrNull()
                    if (role == "caregiver") {
                        firestoreRepository.updateCaregiver(user.uid, mapOf("emailVerified" to true))
                    } else {
                        firestoreRepository.updatePatient(user.uid, mapOf("emailVerified" to true))
                    }
                    
                    _uiState.update { it.copy(isLoading = false, success = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Aún no has verificado tu correo. Revisa tu bandeja de entrada.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al verificar el correo.") }
            }
        }
    }

    /**
     * Resends the verification link.
     */
    fun resendVerificationLink() {
        startTimer()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authService.sendVerificationEmail()
            _uiState.update {
                if (result.isFailure) {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.localizedMessage
                            ?: "Error al reenviar el enlace."
                    )
                } else {
                    it.copy(isLoading = false, success = true)
                }
            }
        }
    }

    /**
     * Sends a password reset email.
     */
    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa un correo electrónico.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
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

    fun ensureCurrentRole() {
        val userId = authService.getCurrentUser()?.uid ?: return
        if (_userRole.value != null) return

        viewModelScope.launch {
            resolveCurrentRole(userId)
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
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = false, emailError = null) }

            val result = authService.resetPassword(email)
            _uiState.update {
                if (result.isFailure) {
                    val exception = result.exceptionOrNull()
                    val message = exception?.localizedMessage.orEmpty()
                    
                    val finalError = if (exception is com.google.firebase.auth.FirebaseAuthInvalidUserException || 
                        "There is no user record" in message || "ERROR_USER_NOT_FOUND" in message) {
                        "El correo no está registrado."
                    } else {
                        exception?.localizedMessage ?: "Error al enviar el correo de recuperación."
                    }
                    
                    it.copy(isLoading = false, error = finalError)
                } else {
                    it.copy(isLoading = false, success = true)
                }
            }
        }
    }

    /** Clears any active error from the UI state. */
    fun clearError() {
        _uiState.update { it.copy(error = null, emailError = null) }
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

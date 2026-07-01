package com.relaxmind.app.features.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.relaxmind.app.data.model.BindingCode
import com.relaxmind.app.data.model.Caregiver
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import com.relaxmind.app.utils.toUserFriendlyMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.random.Random

class LinkCaregiverViewModel(
    private val authService: FirebaseAuthService = FirebaseAuthService(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {
    private val _bindingCode = MutableStateFlow<BindingCode?>(null)
    val bindingCode = _bindingCode.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds = _remainingSeconds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _linked = MutableStateFlow(false)
    val linked = _linked.asStateFlow()

    private val _linkedCaregiver = MutableStateFlow<Caregiver?>(null)
    val linkedCaregiver = _linkedCaregiver.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null
    private var timerJob: Job? = null

    fun createCode(force: Boolean = false) {
        if (!force && _bindingCode.value != null && _remainingSeconds.value > 0) return
        val patientId = authService.getCurrentUser()?.uid
        if (patientId.isNullOrBlank()) {
            _error.value = "No se encontró una sesión activa."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            listenerRegistration?.remove()

            val expiresAt = Date(System.currentTimeMillis() + CODE_TTL_MILLIS)
            val result = firestoreRepository.createBindingCode(
                patientId = patientId,
                code = generateCode(),
                expiresAt = expiresAt
            )

            result.onSuccess { code ->
                _bindingCode.value = code
                startTimer(expiresAt.time)
                listenForLink(code.id)
            }.onFailure { throwable ->
                _error.value = throwable.localizedMessage ?: "No se pudo generar el código."
            }

            _isLoading.value = false
        }
    }

    private fun listenForLink(bindingCodeId: String) {
        listenerRegistration = firestoreRepository.listenToBindingCode(
            bindingCodeId = bindingCodeId,
            onChange = { code ->
                val caregiverId = code?.caregiverId
                if (!caregiverId.isNullOrBlank()) {
                    viewModelScope.launch {
                        firestoreRepository.getCaregiverById(caregiverId)
                            .onSuccess { caregiver -> _linkedCaregiver.value = caregiver }
                        _linked.value = true
                    }
                }
            },
            onError = { exception ->
                _error.value = exception.toUserFriendlyMessage("No se pudo escuchar la vinculación.")
            }
        )
    }

    private fun startTimer(expiresAtMillis: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val remaining = ((expiresAtMillis - System.currentTimeMillis()) / 1000L)
                    .coerceAtLeast(0L)
                    .toInt()
                _remainingSeconds.value = remaining
                if (remaining == 0) break
                delay(1000L)
            }
        }
    }

    private fun generateCode(): String = Random.nextInt(0, 1_000_000).toString().padStart(6, '0')

    fun consumeLinkedConfirmation() {
        _linked.value = false
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        timerJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val CODE_TTL_MILLIS = 10 * 60 * 1000L
    }
}

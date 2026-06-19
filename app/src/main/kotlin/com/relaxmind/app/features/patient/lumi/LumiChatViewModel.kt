package com.relaxmind.app.features.patient.lumi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.content
import com.google.firebase.firestore.ListenerRegistration
import com.relaxmind.app.data.model.LumiMessage
import com.relaxmind.app.data.model.LumiSession
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import com.relaxmind.app.data.remote.GeminiApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

data class LumiChatUiState(
    val isLoading: Boolean = true,
    val isTyping: Boolean = false,
    val sessionId: String? = null,
    val messages: List<LumiMessage> = emptyList(),
    val currentStreamingText: String = "",
    val error: String? = null
)

class LumiChatViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val authService: FirebaseAuthService = FirebaseAuthService(),
    private val geminiApiService: GeminiApiService = GeminiApiService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LumiChatUiState())
    val uiState: StateFlow<LumiChatUiState> = _uiState.asStateFlow()

    private var messagesListener: ListenerRegistration? = null
    var isReadOnly: Boolean = false

    fun initSession(providedSessionId: String? = null) {
        if (providedSessionId != null) {
            isReadOnly = true
            loadSessionMessages(providedSessionId)
        } else {
            viewModelScope.launch {
                val patientId = authService.getCurrentUser()?.uid ?: return@launch
                val sessionResult = firestoreRepository.getActiveLumiSession(patientId)
                val session = sessionResult.getOrNull()

                if (session != null) {
                    loadSessionMessages(session.id)
                } else {
                    val newSession = LumiSession(patientId = patientId)
                    val newSessionId = firestoreRepository.createLumiSession(newSession).getOrNull()
                    if (newSessionId != null) {
                        loadSessionMessages(newSessionId)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "No se pudo crear la sesión")
                    }
                }
            }
        }
    }

    private fun loadSessionMessages(sessionId: String) {
        _uiState.value = _uiState.value.copy(sessionId = sessionId, isLoading = false)
        messagesListener?.remove()
        messagesListener = firestoreRepository.listenToLumiMessages(
            sessionId = sessionId,
            onChange = { msgs ->
                _uiState.value = _uiState.value.copy(messages = msgs)
            },
            onError = {
                _uiState.value = _uiState.value.copy(error = "Error al cargar mensajes")
            }
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || isReadOnly) return
        val sessionId = _uiState.value.sessionId ?: return

        viewModelScope.launch {
            val userMsg = LumiMessage(role = "user", text = text)
            // Firebase local cache allows immediate optimistic update via snapshot
            firestoreRepository.addLumiMessage(sessionId, userMsg)

            _uiState.value = _uiState.value.copy(isTyping = true, currentStreamingText = "")

            val historyContent = _uiState.value.messages.map { 
                content(it.role) { text(it.text) }
            }

            var fullText = ""
            geminiApiService.sendMessage(historyContent, text)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isTyping = false, error = "Error de conexión con Lumi")
                }
                .onCompletion {
                    if (fullText.isNotBlank()) {
                        _uiState.value = _uiState.value.copy(isTyping = false, currentStreamingText = "")
                        val modelMsg = LumiMessage(role = "model", text = fullText)
                        firestoreRepository.addLumiMessage(sessionId, modelMsg)
                    }
                }
                .collect { chunk ->
                    fullText += chunk
                    _uiState.value = _uiState.value.copy(currentStreamingText = fullText)
                }
        }
    }

    fun startNewChat() {
        if (isReadOnly) return
        val currentSessionId = _uiState.value.sessionId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, messages = emptyList())
            firestoreRepository.archiveLumiSession(currentSessionId)
            messagesListener?.remove()
            initSession(null)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
    }
}

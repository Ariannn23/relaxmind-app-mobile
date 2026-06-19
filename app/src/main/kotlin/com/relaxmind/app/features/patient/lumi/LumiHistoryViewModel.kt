package com.relaxmind.app.features.patient.lumi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.relaxmind.app.data.model.LumiSession
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LumiHistoryUiState(
    val isLoading: Boolean = true,
    val sessions: List<LumiSession> = emptyList(),
    val error: String? = null
)

class LumiHistoryViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val authService: FirebaseAuthService = FirebaseAuthService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LumiHistoryUiState())
    val uiState: StateFlow<LumiHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val patientId = authService.getCurrentUser()?.uid ?: return@launch
            val result = firestoreRepository.getLumiSessionsHistory(patientId)
            val sessions = result.getOrNull()

            if (sessions != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, sessions = sessions)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "No se pudo cargar el historial")
            }
        }
    }
}

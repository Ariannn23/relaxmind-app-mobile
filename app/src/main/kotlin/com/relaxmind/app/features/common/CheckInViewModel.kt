package com.relaxmind.app.features.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.relaxmind.app.data.model.BinaryAnswer
import com.relaxmind.app.data.model.CheckIn
import com.relaxmind.app.data.model.FrequencyAnswer
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import com.relaxmind.app.utils.CheckInAnswers
import com.relaxmind.app.utils.WellnessScoreCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

sealed interface CheckInUiState {
    data object Idle : CheckInUiState
    data object Loading : CheckInUiState
    data class Success(val score: Int, val category: String) : CheckInUiState
    data class Error(val message: String) : CheckInUiState
}

class CheckInViewModel(
    private val authService: FirebaseAuthService = FirebaseAuthService(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _emotionalState = MutableStateFlow<Int?>(null)
    val emotionalState = _emotionalState.asStateFlow()

    private val _sleep = MutableStateFlow<Int?>(null)
    val sleep = _sleep.asStateFlow()

    private val _energy = MutableStateFlow(5)
    val energy = _energy.asStateFlow()

    private val _stress = MutableStateFlow(5)
    val stress = _stress.asStateFlow()

    // 4 habits (physical, social, enjoy, routine)
    private val _frequencyAnswers = MutableStateFlow(listOf(3, 3, 3, 3))
    val frequencyAnswers = _frequencyAnswers.asStateFlow()

    // 2 yes/no (concentration, enjoyment)
    private val _binaryAnswers = MutableStateFlow(listOf(-1, -1))
    val binaryAnswers = _binaryAnswers.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _uiState = MutableStateFlow<CheckInUiState>(CheckInUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun selectEmotionalState(state: Int) {
        _emotionalState.value = state
    }

    fun selectSleep(state: Int) {
        _sleep.value = state
    }

    fun setEnergy(value: Int) {
        _energy.value = value
    }

    fun setStress(value: Int) {
        _stress.value = value
    }

    fun setFrequencyAnswer(index: Int, value: Int) {
        _frequencyAnswers.update { current ->
            current.toMutableList().apply { this[index] = value }
        }
    }

    fun setBinaryAnswer(index: Int, value: Int) {
        _binaryAnswers.update { current ->
            current.toMutableList().apply { this[index] = value }
        }
    }

    fun setNotes(value: String) {
        if (value.length <= 500) {
            _notes.value = value
        }
    }

    fun clearState() {
        _emotionalState.value = null
        _sleep.value = null
        _energy.value = 5
        _stress.value = 5
        _frequencyAnswers.value = listOf(3, 3, 3, 3)
        _binaryAnswers.value = listOf(-1, -1)
        _notes.value = ""
        _uiState.value = CheckInUiState.Idle
    }

    fun submitCheckIn(isInitialTest: Boolean) {
        val userId = authService.getCurrentUser()?.uid ?: run {
            _uiState.value = CheckInUiState.Error("No hay sesión activa.")
            return
        }

        viewModelScope.launch {
            _uiState.value = CheckInUiState.Loading

            val emotionalVal = _emotionalState.value ?: 3
            val sleepVal = if (isInitialTest) null else (_sleep.value ?: 3)
            val energyVal = _energy.value
            val stressVal = _stress.value
            val freqList = _frequencyAnswers.value
            val binList = _binaryAnswers.value.map { if (it == -1) 1 else it }
            val noteText = _notes.value

            val scoreAnswers = CheckInAnswers(
                emotionalState = emotionalVal,
                sleep = sleepVal,
                energy = energyVal,
                stress = stressVal,
                frequencyAnswers = freqList,
                binaryAnswers = binList,
                notes = noteText
            )

            val calculatedScore = WellnessScoreCalculator.calculateScore(scoreAnswers)
            val category = WellnessScoreCalculator.getCategory(calculatedScore)
            val dateStr = LocalDate.now().toString()

            val checkIn = CheckIn(
                id = UUID.randomUUID().toString(),
                patientId = userId,
                type = if (isInitialTest) "initial_test" else "daily_checkin",
                date = dateStr,
                score = calculatedScore,
                category = category,
                emotionalState = emotionalVal,
                sleep = sleepVal,
                energy = energyVal,
                stress = stressVal,
                frequencyAnswers = freqList.mapIndexed { index, value ->
                    FrequencyAnswer("freq_$index", value)
                },
                binaryAnswers = binList.mapIndexed { index, value ->
                    BinaryAnswer("bin_$index", value)
                },
                notes = noteText
            )

            val result = firestoreRepository.createCheckIn(checkIn)
            if (result.isSuccess) {
                if (isInitialTest) {
                    firestoreRepository.updatePatient(userId, mapOf("onboardingCompleted" to true))
                }
                
                // Update streak
                val streakResult = firestoreRepository.updatePatientStreak(userId, dateStr)
                
                // Evaluate achievements
                val achievementsResult = firestoreRepository.getPatientAchievements(userId)
                val unlocked = achievementsResult.getOrDefault(emptyList())
                
                com.relaxmind.app.features.patient.AchievementManager.checkAndUnlock(
                    "first_checkin", userId, unlocked, firestoreRepository
                )
                
                if (calculatedScore >= 80) {
                    com.relaxmind.app.features.patient.AchievementManager.checkAndUnlock(
                        "score_80", userId, unlocked, firestoreRepository
                    )
                }
                if (calculatedScore == 100) {
                    com.relaxmind.app.features.patient.AchievementManager.checkAndUnlock(
                        "score_100", userId, unlocked, firestoreRepository
                    )
                }
                
                streakResult.getOrNull()?.let { streakObj ->
                    val currentStreak = streakObj.currentStreak
                    if (currentStreak >= 3) {
                        com.relaxmind.app.features.patient.AchievementManager.checkAndUnlock("streak_3", userId, unlocked, firestoreRepository)
                    }
                    if (currentStreak >= 7) {
                        com.relaxmind.app.features.patient.AchievementManager.checkAndUnlock("streak_7", userId, unlocked, firestoreRepository)
                    }
                    if (currentStreak >= 14) {
                        com.relaxmind.app.features.patient.AchievementManager.checkAndUnlock("streak_14", userId, unlocked, firestoreRepository)
                    }
                    if (currentStreak >= 30) {
                        com.relaxmind.app.features.patient.AchievementManager.checkAndUnlock("streak_30", userId, unlocked, firestoreRepository)
                    }
                }
                
                _uiState.value = CheckInUiState.Success(calculatedScore, category)
            } else {
                _uiState.value = CheckInUiState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Error al guardar el check-in."
                )
            }
        }
    }
}

package com.relaxmind.app.features.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.relaxmind.app.data.model.Appointment
import com.relaxmind.app.data.model.Caregiver
import com.relaxmind.app.data.model.CheckIn
import com.relaxmind.app.data.model.DailyGoal
import com.relaxmind.app.data.model.MeditationExercise
import com.relaxmind.app.data.model.Patient
import com.relaxmind.app.data.model.Streak
import com.relaxmind.app.data.model.UserAchievement
import com.relaxmind.app.data.model.CompletedMeditation
import com.relaxmind.app.data.model.DiaryEntry
import com.relaxmind.app.data.remote.FirebaseAuthService
import com.relaxmind.app.data.remote.FirestoreRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.Date
import android.net.Uri
import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage

class PatientViewModel(
    private val authService: FirebaseAuthService = FirebaseAuthService(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient = _patient.asStateFlow()

    private val _todayCheckIn = MutableStateFlow<CheckIn?>(null)
    val todayCheckIn = _todayCheckIn.asStateFlow()

    private val _dailyGoal = MutableStateFlow<DailyGoal?>(null)
    val dailyGoal = _dailyGoal.asStateFlow()

    private val _dailyGoalExercise = MutableStateFlow<MeditationExercise?>(null)
    val dailyGoalExercise = _dailyGoalExercise.asStateFlow()

    private val _nextAppointment = MutableStateFlow<Appointment?>(null)
    val nextAppointment = _nextAppointment.asStateFlow()

    private val _caregiver = MutableStateFlow<Caregiver?>(null)
    val caregiver = _caregiver.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _streak = MutableStateFlow<Streak?>(null)
    val streak = _streak.asStateFlow()

    private val _achievements = MutableStateFlow<List<UserAchievement>>(emptyList())
    val achievements = _achievements.asStateFlow()

    private val _allCheckIns = MutableStateFlow<List<CheckIn>>(emptyList())
    val allCheckIns = _allCheckIns.asStateFlow()

    private val _meditationExercises = MutableStateFlow<List<MeditationExercise>>(emptyList())
    val meditationExercises = _meditationExercises.asStateFlow()

    private val _selectedExercise = MutableStateFlow<MeditationExercise?>(null)
    val selectedExercise = _selectedExercise.asStateFlow()

    private val _meditationCompleteSuccess = MutableSharedFlow<Pair<String, Int>?>()
    val meditationCompleteSuccess = _meditationCompleteSuccess.asSharedFlow()

    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(LocalDate.now().monthValue)
    val selectedMonth = _selectedMonth.asStateFlow()

    private val _selectedDateAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val selectedDateAppointments = _selectedDateAppointments.asStateFlow()

    private val _monthlyAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val monthlyAppointments = _monthlyAppointments.asStateFlow()

    private val _achievementUnlockedEvent = MutableSharedFlow<UserAchievement>()
    val achievementUnlockedEvent = _achievementUnlockedEvent.asSharedFlow()

    private val _monthlyDiaryEntries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val monthlyDiaryEntries = _monthlyDiaryEntries.asStateFlow()

    private val _diaryEntries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaryEntries = _diaryEntries.asStateFlow()

    private val _selectedAppointment = MutableStateFlow<Appointment?>(null)
    val selectedAppointment = _selectedAppointment.asStateFlow()

    fun loadDashboardData() {
        val userId = authService.getCurrentUser()?.uid ?: run {
            _error.value = "No hay sesión activa."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val patientResult = firestoreRepository.getPatientById(userId)
            if (patientResult.isFailure) {
                _error.value = patientResult.exceptionOrNull()?.localizedMessage ?: "Error al cargar datos del paciente."
                _isLoading.value = false
                return@launch
            }

            val patientData = patientResult.getOrNull()
            _patient.value = patientData
            patientData?.let {
                com.relaxmind.app.ui.themes.ThemeState.darkMode.value = it.darkMode
                com.relaxmind.app.ui.themes.ThemeState.language.value = it.language
            }

            if (patientData == null) {
                _error.value = "Los datos del paciente no existen."
                _isLoading.value = false
                return@launch
            }

            val todayDate = LocalDate.now().toString()

            coroutineScope {
                // Fetch check-in
                val checkInDeferred = async { firestoreRepository.getTodayCheckIn(userId, todayDate) }
                // Fetch daily goal
                val dailyGoalDeferred = async { firestoreRepository.getDailyGoal(userId, todayDate) }
                // Fetch appointments
                val appointmentsDeferred = async { firestoreRepository.getAppointments(userId, todayDate) }
                // Fetch caregiver (if linked)
                val caregiverDeferred = async {
                    patientData.caregiverId?.let {
                        firestoreRepository.getLinkedCaregiverProfile(it, patientData)
                    }
                }

                val checkInResult = checkInDeferred.await()
                val dailyGoalResult = dailyGoalDeferred.await()
                val appointmentsResult = appointmentsDeferred.await()
                val caregiverResult = caregiverDeferred.await()

                // Map CheckIn
                _todayCheckIn.value = checkInResult.getOrNull()

                // Map DailyGoal & Exercise
                var goal = dailyGoalResult.getOrNull()
                var goalExercise: MeditationExercise? = null
                if (goal == null) {
                    val exercises = checkAndSeedExercises()
                    if (exercises.isNotEmpty()) {
                        val randomExercise = exercises.random()
                        val newGoal = DailyGoal(
                            id = UUID.randomUUID().toString(),
                            patientId = userId,
                            date = todayDate,
                            exerciseId = randomExercise.id,
                            completed = false
                        )
                        val saveGoalResult = firestoreRepository.createDailyGoal(newGoal)
                        if (saveGoalResult.isSuccess) {
                            goal = newGoal
                            goalExercise = randomExercise
                        }
                    }
                } else {
                    val exerciseResult = firestoreRepository.getMeditationExercise(goal.exerciseId)
                    goalExercise = exerciseResult.getOrNull() ?: getDefaultExercises().find { it.id == goal.exerciseId }
                }

                _dailyGoal.value = goal
                _dailyGoalExercise.value = goalExercise

                // Map Next Appointment
                val list = appointmentsResult.getOrNull() ?: emptyList()
                _nextAppointment.value = list
                    .filter { !it.completed }
                    .minByOrNull {
                        runCatching {
                            LocalTime.parse(it.time, DateTimeFormatter.ofPattern("HH:mm"))
                        }.getOrDefault(LocalTime.MAX)
                    }

                // Map Caregiver
                val linkedCaregiver = caregiverResult?.getOrNull()
                _caregiver.value = linkedCaregiver
                if (patientData.caregiverId != null && linkedCaregiver == null) {
                    _error.value = caregiverResult?.exceptionOrNull()?.localizedMessage
                        ?: "No se pudieron cargar los datos del cuidador vinculado."
                }
            }

            _isLoading.value = false
        }
    }

    fun toggleDailyGoalCompletion(completed: Boolean) {
        val goal = _dailyGoal.value ?: return
        viewModelScope.launch {
            val result = firestoreRepository.updateDailyGoalCompletion(goal.id, completed)
            if (result.isSuccess) {
                _dailyGoal.update { it?.copy(completed = completed) }
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Error al actualizar la meta."
            }
        }
    }

    private fun updatePatientField(fieldName: String, value: Any, updateLocal: (Patient) -> Patient) {
        val userId = authService.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _patient.update { it?.let(updateLocal) }
            val result = firestoreRepository.updatePatient(userId, mapOf(fieldName to value))
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Error al actualizar."
                loadDashboardData()
            }
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        com.relaxmind.app.ui.themes.ThemeState.darkMode.value = enabled
        updatePatientField("darkMode", enabled) { it.copy(darkMode = enabled) }
    }

    fun updateLanguage(lang: String) {
        com.relaxmind.app.ui.themes.ThemeState.language.value = lang
        updatePatientField("language", lang) { it.copy(language = lang) }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        updatePatientField("notificationsEnabled", enabled) { it.copy(notificationsEnabled = enabled) }
    }

    fun updateCheckInReminderEnabled(enabled: Boolean) {
        updatePatientField("checkInReminderEnabled", enabled) { it.copy(checkInReminderEnabled = enabled) }
    }

    fun updateBiometricEnabled(enabled: Boolean) {
        updatePatientField("biometricEnabled", enabled) { it.copy(biometricEnabled = enabled) }
    }

    fun updateProfile(
        name: String,
        lastName: String,
        birthDate: String,
        sex: String,
        phone: String,
        condition: String,
        avatarUrl: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = authService.getCurrentUser()?.uid ?: run {
            onError("No hay sesión activa.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val currentAvatar = _patient.value?.avatarUrl ?: "relaxmind://avatar/buho"
            val updatedAvatarUrl = avatarUrl.ifBlank { currentAvatar }
            
            val fields = mapOf(
                "name" to name,
                "lastName" to lastName,
                "birthDate" to birthDate,
                "sex" to sex,
                "phone" to phone,
                "condition" to condition,
                "avatarUrl" to updatedAvatarUrl
            )
            _patient.update { it?.copy(
                name = name, 
                lastName = lastName, 
                birthDate = birthDate, 
                sex = sex, 
                phone = phone, 
                condition = condition,
                avatarUrl = updatedAvatarUrl
            ) }
            val result = firestoreRepository.updatePatient(userId, fields)
            _isLoading.value = false
            if (result.isSuccess) onSuccess()
            else onError(result.exceptionOrNull()?.localizedMessage ?: "Error al guardar el perfil.")
        }
    }

    fun unlinkCaregiver(passwordConfirm: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = authService.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val reauthResult = authService.reauthenticate(passwordConfirm)
            if (reauthResult.isFailure) {
                val errorMsg = reauthResult.exceptionOrNull()?.localizedMessage ?: "Contraseña incorrecta."
                onError(errorMsg)
                _isLoading.value = false
                return@launch
            }

            val updateResult = firestoreRepository.updatePatient(
                userId,
                mapOf<String, Any?>(
                    "caregiverId" to null,
                    "linkedCaregiverAt" to null
                )
            )
            if (updateResult.isSuccess) {
                _patient.update { it?.copy(caregiverId = null, linkedCaregiverAt = null) }
                _caregiver.value = null
                onSuccess()
            } else {
                val errorMsg = updateResult.exceptionOrNull()?.localizedMessage ?: "Error al desvincular."
                onError(errorMsg)
            }
            _isLoading.value = false
        }
    }

    fun deleteAccount(
        reason: String,
        passwordConfirm: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = authService.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val reauthResult = authService.reauthenticate(passwordConfirm)
            if (reauthResult.isFailure) {
                val errorMsg = reauthResult.exceptionOrNull()?.localizedMessage ?: "Contraseña incorrecta."
                onError(errorMsg)
                _isLoading.value = false
                return@launch
            }

            val todayDate = LocalDate.now().toString()
            val updateResult = firestoreRepository.updatePatient(
                userId,
                mapOf(
                    "isDeleted" to true,
                    "deletedAt" to todayDate,
                    "deletionReason" to reason
                )
            )
            if (updateResult.isSuccess) {
                authService.logout()
                onSuccess()
            } else {
                val errorMsg = updateResult.exceptionOrNull()?.localizedMessage ?: "Error al borrar la cuenta."
                onError(errorMsg)
            }
            _isLoading.value = false
        }
    }

    fun loadProgressData() {
        val userId = authService.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val streakResult = firestoreRepository.getPatientStreak(userId)
            val achievementsResult = firestoreRepository.getPatientAchievements(userId)
            val checkInsResult = firestoreRepository.getPatientCheckIns(userId)

            _streak.value = streakResult.getOrNull()
            _achievements.value = achievementsResult.getOrNull() ?: emptyList()
            _allCheckIns.value = (checkInsResult.getOrNull() ?: emptyList()).sortedByDescending { it.date }

            _isLoading.value = false
        }
    }

    fun selectPreviousMonth() {
        if (_selectedMonth.value == 1) {
            _selectedMonth.value = 12
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
    }

    fun selectNextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedMonth.value = 1
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
    }

    private fun getDefaultExercises(): List<MeditationExercise> {
        return listOf(
            MeditationExercise(
                id = "resp_478",
                title = "Respiración 4-7-8",
                description = "Un ejercicio clásico de respiración profunda para relajar el sistema nervioso.",
                type = "respiracion",
                durationMinutes = 5,
                lottieAnimationUrl = "",
                order = 1
            ),
            MeditationExercise(
                id = "resp_caja",
                title = "Respiración de caja",
                description = "Técnica utilizada por profesionales para recuperar la calma y enfoque.",
                type = "respiracion",
                durationMinutes = 4,
                lottieAnimationUrl = "",
                order = 2
            ),
            MeditationExercise(
                id = "body_scan",
                title = "Escaneo corporal",
                description = "Recorre mentalmente tu cuerpo para liberar la tensión física acumulada.",
                type = "relajacion",
                durationMinutes = 10,
                lottieAnimationUrl = "",
                order = 3
            ),
            MeditationExercise(
                id = "gratitud",
                title = "Meditación de gratitud",
                description = "Enfoca tu mente en el aprecio y agradecimiento del momento presente.",
                type = "mindfulness",
                durationMinutes = 7,
                lottieAnimationUrl = "",
                order = 4
            ),
            MeditationExercise(
                id = "resp_diafragmatica",
                title = "Respiración diafragmática",
                description = "Respiración abdominal profunda para inducir una respuesta rápida de relajación.",
                type = "respiracion",
                durationMinutes = 6,
                lottieAnimationUrl = "",
                order = 5
            )
        )
    }

    private suspend fun checkAndSeedExercises(): List<MeditationExercise> {
        val result = firestoreRepository.getMeditationExercises()
        var exercises = result.getOrDefault(emptyList())
        if (exercises.isEmpty()) {
            val defaults = getDefaultExercises()
            for (exercise in defaults) {
                firestoreRepository.createMeditationExercise(exercise)
            }
            exercises = defaults
        }
        return exercises
    }

    fun loadMeditationExercises() {
        viewModelScope.launch {
            _isLoading.value = true
            val exercises = checkAndSeedExercises()
            _meditationExercises.value = exercises
            _isLoading.value = false
        }
    }

    fun loadMeditationExercise(exerciseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = firestoreRepository.getMeditationExercise(exerciseId)
            var exercise = result.getOrNull()
            if (exercise == null) {
                exercise = getDefaultExercises().find { it.id == exerciseId }
            }
            _selectedExercise.value = exercise
            _isLoading.value = false
        }
    }

    fun completeMeditation(exerciseId: String) {
        val userId = authService.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            
            // 1. Check if matches today's daily goal
            val isGoal = _dailyGoal.value?.let { goal ->
                goal.exerciseId == exerciseId && !goal.completed
            } ?: false

            // 2. Save CompletedMeditation
            val completionId = UUID.randomUUID().toString()
            val completedMeditation = CompletedMeditation(
                id = completionId,
                patientId = userId,
                exerciseId = exerciseId,
                isGoalOfTheDay = isGoal,
                completedAt = Date()
            )

            val saveResult = firestoreRepository.createCompletedMeditation(completedMeditation)
            if (saveResult.isSuccess) {
                // 3. Update daily goal if matches
                if (isGoal) {
                    _dailyGoal.value?.let { goal ->
                        firestoreRepository.updateDailyGoalCompletion(goal.id, true)
                        _dailyGoal.value = goal.copy(completed = true)
                    }
                }

                // 4. Verify achievements
                val completionsResult = firestoreRepository.getCompletedMeditations(userId)
                if (completionsResult.isSuccess) {
                    val completions = completionsResult.getOrDefault(emptyList())
                    val totalCompletions = completions.size
                    val achievementsResult = firestoreRepository.getPatientAchievements(userId)
                    val unlockedAchievements = achievementsResult.getOrDefault(emptyList())

                    if (totalCompletions >= 1) {
                        AchievementManager.checkAndUnlock(
                            "first_meditation",
                            userId,
                            unlockedAchievements,
                            firestoreRepository
                        ) { ach -> _achievements.value = _achievements.value + ach }
                    }
                    if (totalCompletions >= 10) {
                        AchievementManager.checkAndUnlock(
                            "meditations_10",
                            userId,
                            unlockedAchievements,
                            firestoreRepository
                        ) { ach -> _achievements.value = _achievements.value + ach }
                    }
                }

                _meditationCompleteSuccess.emit(Pair(exerciseId, 50))
            }
            _isLoading.value = false
        }
    }

    fun resetMeditationCompleteSuccess() {
        viewModelScope.launch {
            _meditationCompleteSuccess.emit(null)
        }
    }

    fun loadAppointmentsForDate(date: String) {
        val userId = authService.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = firestoreRepository.getAppointments(userId, date)
            if (result.isSuccess) {
                _selectedDateAppointments.value = result.getOrDefault(emptyList()).sortedBy { it.time }
            }
            _isLoading.value = false
        }
    }

    fun loadAppointmentDetail(appointmentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = firestoreRepository.getAppointment(appointmentId)
            if (result.isSuccess) {
                _selectedAppointment.value = result.getOrNull()
            }
            _isLoading.value = false
        }
    }

    fun loadMonthlyEvents(year: Int, month: Int) {
        val userId = authService.getCurrentUser()?.uid ?: return
        val yearMonth = String.format("%d-%02d", year, month)
        viewModelScope.launch {
            _isLoading.value = true
            val apptsResult = firestoreRepository.getAppointmentsForMonth(userId, yearMonth)
            val diariesResult = firestoreRepository.getDiaryEntriesForMonth(userId, yearMonth)
            
            if (apptsResult.isSuccess) {
                _monthlyAppointments.value = apptsResult.getOrDefault(emptyList())
            }
            if (diariesResult.isSuccess) {
                _monthlyDiaryEntries.value = diariesResult.getOrDefault(emptyList())
            }
            _isLoading.value = false
        }
    }

    fun loadDiaryEntries() {
        val userId = authService.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = firestoreRepository.getDiaryEntries(userId)
            if (result.isSuccess) {
                _diaryEntries.value = result.getOrDefault(emptyList())
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage
                    ?: "Error al cargar el diario."
            }

            _isLoading.value = false
        }
    }

    fun createAppointment(
        title: String,
        type: String,
        category: String,
        date: String,
        time: String,
        reminderMinutes: Int,
        notes: String,
        recurring: Boolean = false,
        recurringDays: List<Int> = emptyList(),
        context: Context,
        onSuccess: () -> Unit
    ) {
        val userId = authService.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val appointmentId = UUID.randomUUID().toString()
            val appointment = Appointment(
                id = appointmentId,
                patientId = userId,
                title = title,
                type = type,
                category = category,
                date = date,
                time = time,
                reminderTime = reminderMinutes,
                completed = false,
                notificationSent = false,
                notes = notes,
                recurring = recurring,
                recurringDays = recurringDays,
                createdAt = Date()
            )
            
            val result = firestoreRepository.createAppointment(appointment)
            if (result.isSuccess) {
                // Programar WorkManager reminder
                scheduleAppointmentReminder(context, appointment)
                onSuccess()
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Error al guardar el evento."
            }
            _isLoading.value = false
        }
    }

    private fun getDelayForNextOccurrence(targetDayOfWeek: Int, timeStr: String, reminderMinutes: Int): Long {
        val today = LocalDate.now()
        val time = runCatching { LocalTime.parse(timeStr) }.getOrDefault(LocalTime.of(10, 30))
        
        var targetDate = today
        val currentDayOfWeek = today.dayOfWeek.value // 1 = Mon, 7 = Sun
        
        val daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7
        if (daysToAdd == 0) {
            val appointmentTimeMinusReminder = time.minusMinutes(reminderMinutes.toLong())
            val nowTime = LocalTime.now()
            if (nowTime.isAfter(appointmentTimeMinusReminder)) {
                targetDate = today.plusDays(7)
            }
        } else {
            targetDate = today.plusDays(daysToAdd.toLong())
        }
        
        val localDateTime = java.time.LocalDateTime.of(targetDate, time)
        val targetMs = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val reminderMs = targetMs - (reminderMinutes * 60 * 1000)
        return reminderMs - System.currentTimeMillis()
    }

    private fun scheduleAppointmentReminder(context: Context, appointment: Appointment) {
        runCatching {
            if (appointment.recurring) {
                appointment.recurringDays.forEach { dayOfWeek ->
                    val delayMs = getDelayForNextOccurrence(dayOfWeek, appointment.time, appointment.reminderTime)
                    if (delayMs > 0) {
                        val data = workDataOf(
                            "appointmentId" to appointment.id,
                            "title" to appointment.title,
                            "type" to appointment.type,
                            "dayOfWeek" to dayOfWeek
                        )
                        
                        val request = OneTimeWorkRequestBuilder<com.relaxmind.app.utils.AppointmentReminderWorker>()
                            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                            .setInputData(data)
                            .addTag("appointment_${appointment.id}")
                            .addTag("appointment_${appointment.id}_$dayOfWeek")
                            .build()
                        
                        WorkManager.getInstance(context).enqueueUniqueWork(
                            "appointment_${appointment.id}_$dayOfWeek",
                            androidx.work.ExistingWorkPolicy.REPLACE,
                            request
                        )
                    }
                }
            } else {
                val dateTimeStr = "${appointment.date}T${appointment.time}"
                val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val localDateTime = java.time.LocalDateTime.parse(dateTimeStr, formatter)
                val appointmentMs = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val targetMs = appointmentMs - (appointment.reminderTime * 60 * 1000)
                val delayMs = targetMs - System.currentTimeMillis()
                
                if (delayMs > 0) {
                    val data = workDataOf(
                        "appointmentId" to appointment.id,
                        "title" to appointment.title,
                        "type" to appointment.type,
                        "dayOfWeek" to -1
                    )
                    
                    val request = OneTimeWorkRequestBuilder<com.relaxmind.app.utils.AppointmentReminderWorker>()
                        .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .addTag("appointment_${appointment.id}")
                        .build()
                    
                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "appointment_${appointment.id}",
                        androidx.work.ExistingWorkPolicy.REPLACE,
                        request
                    )
                }
            }
        }
    }

    fun updateAppointmentCompletion(appointmentId: String, completed: Boolean, date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = firestoreRepository.updateAppointmentCompletion(appointmentId, completed)
            if (result.isSuccess) {
                loadAppointmentsForDate(date)
                val parts = date.split("-")
                if (parts.size == 3) {
                    val year = parts[0].toIntOrNull() ?: LocalDate.now().year
                    val month = parts[1].toIntOrNull() ?: LocalDate.now().monthValue
                    loadMonthlyEvents(year, month)
                }
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Error al actualizar el evento."
            }
            _isLoading.value = false
        }
    }

    fun deleteAppointment(appointmentId: String, date: String, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = firestoreRepository.deleteAppointment(appointmentId)
            if (result.isSuccess) {
                runCatching {
                    WorkManager.getInstance(context).cancelAllWorkByTag("appointment_$appointmentId")
                }
                loadAppointmentsForDate(date)
                val parts = date.split("-")
                if (parts.size == 3) {
                    val year = parts[0].toIntOrNull() ?: LocalDate.now().year
                    val month = parts[1].toIntOrNull() ?: LocalDate.now().monthValue
                    loadMonthlyEvents(year, month)
                }
                onSuccess()
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Error al eliminar el evento."
            }
            _isLoading.value = false
        }
    }

    fun createDiaryEntry(
        category: String,
        emotion: String,
        notes: String,
        localPhotoUris: List<Uri>,
        context: Context,
        onSuccess: () -> Unit
    ) {
        val userId = authService.getCurrentUser()?.uid ?: return
        val todayDate = LocalDate.now().toString()
        viewModelScope.launch {
            _isLoading.value = true
            
            val uploadedUrls = mutableListOf<String>()
            localPhotoUris.take(5).forEachIndexed { index, uri ->
                val url = compressAndUploadImage(uri, context, userId, index)
                if (url != null) {
                    uploadedUrls.add(url)
                }
            }
            
            val entryId = UUID.randomUUID().toString()
            val diaryEntry = DiaryEntry(
                id = entryId,
                patientId = userId,
                category = category,
                emotion = emotion,
                notes = notes,
                photoUrls = uploadedUrls,
                date = todayDate,
                createdAt = Date()
            )
            
            val result = firestoreRepository.createDiaryEntry(diaryEntry)
            if (result.isSuccess) {
                _diaryEntries.update { it + diaryEntry } // Actualización local inmediata
                checkDiaryAchievements(userId)
                onSuccess()
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Error al guardar la entrada del diario."
            }
            _isLoading.value = false
        }
    }

    private suspend fun checkDiaryAchievements(userId: String) {
        val countResult = firestoreRepository.getDiaryEntriesCount(userId)
        val achievementsResult = firestoreRepository.getPatientAchievements(userId)
        
        val totalEntries = countResult.getOrDefault(0)
        val unlockedAchievements = achievementsResult.getOrDefault(emptyList())
        
        if (totalEntries >= 1) {
            AchievementManager.checkAndUnlock(
                "first_diary",
                userId,
                unlockedAchievements,
                firestoreRepository
            ) { ach -> _achievements.value = _achievements.value + ach }
        }
        if (totalEntries >= 7) {
            AchievementManager.checkAndUnlock(
                "diary_7",
                userId,
                unlockedAchievements,
                firestoreRepository
            ) { ach -> _achievements.value = _achievements.value + ach }
        }
    }

    private suspend fun compressAndUploadImage(uri: Uri, context: Context, userId: String, index: Int): String? {
        return runCatching {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            
            val maxSide = 1280
            val width = originalBitmap.width
            val height = originalBitmap.height
            val (newWidth, newHeight) = if (width > height) {
                if (width > maxSide) {
                    Pair(maxSide, (height * (maxSide.toFloat() / width)).toInt())
                } else Pair(width, height)
            } else {
                if (height > maxSide) {
                    Pair((width * (maxSide.toFloat() / height)).toInt(), maxSide)
                } else Pair(width, height)
            }
            
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val data = outputStream.toByteArray()
            
            val fileName = "${System.currentTimeMillis()}_$index.jpg"
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReference("diary_photos/$userId/$fileName")
            storageRef.putBytes(data).await()
            storageRef.downloadUrl.await().toString()
        }.getOrNull()
    }

    fun logout() {
        authService.logout()
    }

    fun clearError() {
        _error.value = null
    }
}

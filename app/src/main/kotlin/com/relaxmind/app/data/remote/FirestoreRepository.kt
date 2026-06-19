package com.relaxmind.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.relaxmind.app.data.model.Appointment
import com.relaxmind.app.data.model.BindingCode
import com.relaxmind.app.data.model.Caregiver
import com.relaxmind.app.data.model.CaregiverAlert
import com.relaxmind.app.data.model.CheckIn
import com.relaxmind.app.data.model.DailyGoal
import com.relaxmind.app.data.model.MeditationExercise
import com.relaxmind.app.data.model.CompletedMeditation
import com.relaxmind.app.data.model.DiaryEntry
import com.relaxmind.app.data.model.Patient
import com.relaxmind.app.data.model.Streak
import com.relaxmind.app.data.model.UserAchievement
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.Date

class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val patients = firestore.collection(PATIENTS_COLLECTION)
    private val caregivers = firestore.collection(CAREGIVERS_COLLECTION)
    private val bindingCodes = firestore.collection(BINDING_CODES_COLLECTION)

    suspend fun createPatient(patient: Patient): Result<Unit> = runCatching {
        require(patient.id.isNotBlank()) { "Patient id cannot be blank." }
        patients.document(patient.id).set(patient).await()
    }

    suspend fun createCaregiver(caregiver: Caregiver): Result<Unit> = runCatching {
        require(caregiver.id.isNotBlank()) { "Caregiver id cannot be blank." }
        caregivers.document(caregiver.id).set(caregiver).await()
    }

    suspend fun getPatientById(id: String): Result<Patient?> = runCatching {
        patients.document(id).get().await().toObject(Patient::class.java)
    }

    suspend fun getCaregiverById(id: String): Result<Caregiver?> = runCatching {
        caregivers.document(id).get().await().toObject(Caregiver::class.java)
    }

    suspend fun updatePatient(
        id: String,
        fields: Map<String, Any?>
    ): Result<Unit> = runCatching {
        patients.document(id).update(fields).await()
    }

    suspend fun updateCaregiver(
        id: String,
        fields: Map<String, Any?>
    ): Result<Unit> = runCatching {
        caregivers.document(id).update(fields).await()
    }

    suspend fun getRoleById(id: String): Result<String> = runCatching {
        val patientSnapshot = patients.document(id).get().await()
        if (patientSnapshot.exists()) {
            return@runCatching patientSnapshot.getString("role") ?: "patient"
        }

        val caregiverSnapshot = caregivers.document(id).get().await()
        if (caregiverSnapshot.exists()) {
            return@runCatching caregiverSnapshot.getString("role") ?: "caregiver"
        }

        error("No user role found for id: $id")
    }

    suspend fun createBindingCode(
        patientId: String,
        code: String,
        expiresAt: Date
    ): Result<BindingCode> = runCatching {
        require(patientId.isNotBlank()) { "Patient id cannot be blank." }
        require(code.length == 6) { "Binding code must have six digits." }

        val document = bindingCodes.document()
        val bindingCode = BindingCode(
            id = document.id,
            code = code,
            patientId = patientId,
            expiresAt = expiresAt
        )
        document.set(bindingCode).await()
        bindingCode
    }

    fun listenToBindingCode(
        bindingCodeId: String,
        onChange: (BindingCode?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return bindingCodes.document(bindingCodeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                onChange(snapshot?.toObject(BindingCode::class.java))
            }
    }

    fun listenPatientsForCaregiver(
        caregiverId: String,
        onChange: (List<Patient>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return patients
            .whereEqualTo("caregiverId", caregiverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onChange(snapshot?.toObjectList(Patient::class.java).orEmpty())
            }
    }

    fun listenAlertsForCaregiver(
        caregiverId: String,
        limit: Long = 10,
        onChange: (List<CaregiverAlert>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection(ALERTS_COLLECTION)
            .whereEqualTo("caregiverId", caregiverId)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val alerts = snapshot?.toObjectList(CaregiverAlert::class.java).orEmpty()
                    .sortedByDescending { it.createdAt?.time ?: 0L }
                onChange(alerts)
            }
    }

    fun listenAlertsForPatient(
        caregiverId: String,
        patientId: String,
        onChange: (List<CaregiverAlert>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection(ALERTS_COLLECTION)
            .whereEqualTo("caregiverId", caregiverId)
            .whereEqualTo("patientId", patientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val alerts = snapshot?.toObjectList(CaregiverAlert::class.java).orEmpty()
                    .sortedByDescending { it.createdAt?.time ?: 0L }
                onChange(alerts)
            }
    }

    suspend fun createAlert(alert: CaregiverAlert): Result<String> = runCatching {
        val document = firestore.collection(ALERTS_COLLECTION).document(
            if (alert.id.isNotBlank()) alert.id else firestore.collection(ALERTS_COLLECTION).document().id
        )
        document.set(alert.copy(id = document.id)).await()
        document.id
    }

    suspend fun updateAlertLocation(alertId: String, latitude: Double, longitude: Double): Result<Unit> = runCatching {
        firestore.collection(ALERTS_COLLECTION)
            .document(alertId)
            .update(
                mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude
                )
            )
            .await()
    }

    fun listenToAlert(
        alertId: String,
        onChange: (CaregiverAlert?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection(ALERTS_COLLECTION)
            .document(alertId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onChange(snapshot?.toObject(CaregiverAlert::class.java))
            }
    }

    suspend fun linkPatientWithCode(
        code: String,
        caregiverId: String
    ): Result<String> = runCatching {
        val now = Date()
        val bindingSnapshot = bindingCodes
            .whereEqualTo("code", code)
            .whereGreaterThan("expiresAt", now)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?: error("Código inválido o expirado")

        val bindingCode = bindingSnapshot.toObject(BindingCode::class.java)
            ?: error("Código inválido o expirado")
        val patientRef = patients.document(bindingCode.patientId)
        val patient = patientRef.get().await().toObject(Patient::class.java)
            ?: error("Paciente no encontrado")

        if (!patient.caregiverId.isNullOrBlank()) {
            error("Este paciente ya está vinculado a un cuidador. El paciente debe desvincularse primero.")
        }

        val linkedAt = LocalDate.now().toString()
        firestore.runBatch { batch ->
            batch.update(
                patientRef,
                mapOf(
                    "caregiverId" to caregiverId,
                    "linkedCaregiverAt" to linkedAt
                )
            )
            batch.update(bindingSnapshot.reference, "caregiverId", caregiverId)
        }.await()

        bindingCode.patientId
    }

    suspend fun getPatientsForCaregiver(caregiverId: String): Result<List<Patient>> = runCatching {
        patients
            .whereEqualTo("caregiverId", caregiverId)
            .get()
            .await()
            .toObjectList(Patient::class.java)
    }

    suspend fun getActiveCaregiverAlerts(caregiverId: String): Result<List<CaregiverAlert>> = runCatching {
        firestore.collection(ALERTS_COLLECTION)
            .whereEqualTo("caregiverId", caregiverId)
            .whereEqualTo("resolved", false)
            .get()
            .await()
            .toObjectList(CaregiverAlert::class.java)
            .sortedByDescending { it.createdAt?.time ?: 0L }
    }

    suspend fun updateAlertResolved(alertId: String, resolved: Boolean): Result<Unit> = runCatching {
        firestore.collection(ALERTS_COLLECTION)
            .document(alertId)
            .update("resolved", resolved)
            .await()
    }

    suspend fun getLatestCheckIn(patientId: String): Result<CheckIn?> = runCatching {
        firestore.collection(CHECKINS_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .get()
            .await()
            .toObjectList(CheckIn::class.java)
            .maxByOrNull { it.date }
    }

    suspend fun createCheckIn(checkIn: CheckIn): Result<Unit> = runCatching {
        firestore.collection(CHECKINS_COLLECTION)
            .document(checkIn.id.ifBlank { firestore.collection(CHECKINS_COLLECTION).document().id })
            .set(checkIn)
            .await()
    }

    suspend fun getTodayCheckIn(patientId: String, date: String): Result<CheckIn?> = runCatching {
        firestore.collection(CHECKINS_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("date", date)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(CheckIn::class.java)
    }

    suspend fun getDailyGoal(patientId: String, date: String): Result<DailyGoal?> = runCatching {
        firestore.collection(DAILY_GOALS_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("date", date)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(DailyGoal::class.java)
    }

    suspend fun createDailyGoal(dailyGoal: DailyGoal): Result<Unit> = runCatching {
        firestore.collection(DAILY_GOALS_COLLECTION)
            .document(dailyGoal.id.ifBlank { firestore.collection(DAILY_GOALS_COLLECTION).document().id })
            .set(dailyGoal)
            .await()
    }

    suspend fun getMeditationExercise(exerciseId: String): Result<MeditationExercise?> = runCatching {
        firestore.collection(MEDITATION_EXERCISES_COLLECTION)
            .document(exerciseId)
            .get()
            .await()
            .toObject(MeditationExercise::class.java)
    }

    suspend fun createMeditationExercise(exercise: MeditationExercise): Result<Unit> = runCatching {
        firestore.collection(MEDITATION_EXERCISES_COLLECTION)
            .document(exercise.id)
            .set(exercise)
            .await()
    }

    suspend fun getAppointments(patientId: String, date: String): Result<List<Appointment>> = runCatching {
        val targetDate = runCatching { LocalDate.parse(date) }.getOrNull()
        val targetDayOfWeek = targetDate?.dayOfWeek?.value ?: -1

        val specific = firestore.collection(APPOINTMENTS_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("date", date)
            .get()
            .await()
            .toObjectList(Appointment::class.java)

        val recurring = firestore.collection(APPOINTMENTS_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("recurring", true)
            .get()
            .await()
            .toObjectList(Appointment::class.java)

        val matchingRecurring = if (targetDate != null && targetDayOfWeek != -1) {
            recurring.filter { appt ->
                val apptStartDate = runCatching { LocalDate.parse(appt.date) }.getOrNull()
                apptStartDate != null && !targetDate.isBefore(apptStartDate) && appt.recurringDays.contains(targetDayOfWeek)
            }
        } else {
            emptyList()
        }

        (specific + matchingRecurring).distinctBy { it.id }
    }

    suspend fun updateDailyGoalCompletion(goalId: String, completed: Boolean): Result<Unit> = runCatching {
        firestore.collection(DAILY_GOALS_COLLECTION)
            .document(goalId)
            .update("completed", completed)
            .await()
    }

    suspend fun getPatientStreak(patientId: String): Result<Streak?> = runCatching {
        firestore.collection(STREAKS_COLLECTION)
            .document(patientId)
            .get()
            .await()
            .toObject(Streak::class.java)?.copy(patientId = patientId)
    }

    suspend fun getPatientAchievements(patientId: String): Result<List<UserAchievement>> = runCatching {
        firestore.collection(ACHIEVEMENTS_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .get()
            .await()
            .toObjectList(UserAchievement::class.java)
    }

    suspend fun getPatientCheckIns(patientId: String): Result<List<CheckIn>> = runCatching {
        firestore.collection(CHECKINS_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .get()
            .await()
            .toObjectList(CheckIn::class.java)
    }

    suspend fun getMeditationExercises(): Result<List<MeditationExercise>> = runCatching {
        firestore.collection(MEDITATION_EXERCISES_COLLECTION)
            .orderBy("order")
            .get()
            .await()
            .toObjectList(MeditationExercise::class.java)
    }

    suspend fun createCompletedMeditation(completedMeditation: CompletedMeditation): Result<Unit> = runCatching {
        firestore.collection(COMPLETED_MEDITATIONS_COLLECTION)
            .document(completedMeditation.id.ifBlank { firestore.collection(COMPLETED_MEDITATIONS_COLLECTION).document().id })
            .set(completedMeditation)
            .await()
    }

    suspend fun getCompletedMeditations(patientId: String): Result<List<CompletedMeditation>> = runCatching {
        firestore.collection(COMPLETED_MEDITATIONS_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .get()
            .await()
            .toObjectList(CompletedMeditation::class.java)
    }

    suspend fun unlockAchievement(userAchievement: UserAchievement): Result<Unit> = runCatching {
        firestore.collection(ACHIEVEMENTS_COLLECTION)
            .document(userAchievement.id.ifBlank { firestore.collection(ACHIEVEMENTS_COLLECTION).document().id })
            .set(userAchievement)
            .await()
    }

    suspend fun createAppointment(appointment: Appointment): Result<Unit> = runCatching {
        firestore.collection(APPOINTMENTS_COLLECTION)
            .document(appointment.id.ifBlank { firestore.collection(APPOINTMENTS_COLLECTION).document().id })
            .set(appointment)
            .await()
    }

    suspend fun getAppointment(appointmentId: String): Result<Appointment?> = runCatching {
        firestore.collection(APPOINTMENTS_COLLECTION)
            .document(appointmentId)
            .get()
            .await()
            .toObject(Appointment::class.java)
    }

    suspend fun updateAppointmentCompletion(appointmentId: String, completed: Boolean): Result<Unit> = runCatching {
        firestore.collection(APPOINTMENTS_COLLECTION)
            .document(appointmentId)
            .update("completed", completed)
            .await()
    }

    suspend fun deleteAppointment(appointmentId: String): Result<Unit> = runCatching {
        firestore.collection(APPOINTMENTS_COLLECTION)
            .document(appointmentId)
            .delete()
            .await()
    }

    suspend fun getAppointmentsForMonth(patientId: String, yearMonth: String): Result<List<Appointment>> = runCatching {
        val parts = yearMonth.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val startLocalDate = LocalDate.of(year, month, 1)
        val daysInMonth = startLocalDate.lengthOfMonth()

        val specific = firestore.collection(APPOINTMENTS_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .whereGreaterThanOrEqualTo("date", "$yearMonth-01")
            .whereLessThanOrEqualTo("date", "$yearMonth-31")
            .get()
            .await()
            .toObjectList(Appointment::class.java)

        val recurring = firestore.collection(APPOINTMENTS_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("recurring", true)
            .get()
            .await()
            .toObjectList(Appointment::class.java)

        val expandedList = mutableListOf<Appointment>()
        expandedList.addAll(specific)

        for (day in 1..daysInMonth) {
            val currentDate = LocalDate.of(year, month, day)
            val currentDayOfWeek = currentDate.dayOfWeek.value // 1 = Mon, 7 = Sun
            val dateStr = currentDate.toString()

            val activeRecurring = recurring.filter { appt ->
                val apptStartDate = runCatching { LocalDate.parse(appt.date) }.getOrNull()
                apptStartDate != null && !currentDate.isBefore(apptStartDate) && appt.recurringDays.contains(currentDayOfWeek)
            }

            activeRecurring.forEach { appt ->
                val alreadyExists = specific.any { it.id == appt.id && it.date == dateStr }
                if (!alreadyExists) {
                    expandedList.add(appt.copy(date = dateStr))
                }
            }
        }

        expandedList.distinctBy { "${it.id}_${it.date}" }
    }

    suspend fun createDiaryEntry(diaryEntry: DiaryEntry): Result<Unit> = runCatching {
        firestore.collection(DIARY_ENTRIES_COLLECTION)
            .document(diaryEntry.id.ifBlank { firestore.collection(DIARY_ENTRIES_COLLECTION).document().id })
            .set(diaryEntry)
            .await()
    }

    suspend fun getDiaryEntriesForMonth(patientId: String, yearMonth: String): Result<List<DiaryEntry>> = runCatching {
        firestore.collection(DIARY_ENTRIES_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .whereGreaterThanOrEqualTo("date", "$yearMonth-01")
            .whereLessThanOrEqualTo("date", "$yearMonth-31")
            .get()
            .await()
            .toObjectList(DiaryEntry::class.java)
    }

    suspend fun getDiaryEntries(patientId: String): Result<List<DiaryEntry>> = runCatching {
        firestore.collection(DIARY_ENTRIES_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .get()
            .await()
            .toObjectList(DiaryEntry::class.java)
            .sortedByDescending { it.date }
    }

    suspend fun getDiaryEntriesCount(patientId: String): Result<Int> = runCatching {
        firestore.collection(DIARY_ENTRIES_COLLECTION)
            .whereEqualTo("patientId", patientId)
            .get()
            .await()
            .size()
    }

    private fun <T> com.google.firebase.firestore.QuerySnapshot.toObjectList(clazz: Class<T>): List<T> {
        return documents.mapNotNull { it.toObject(clazz) }
    }

    private companion object {
        const val PATIENTS_COLLECTION = "patients"
        const val CAREGIVERS_COLLECTION = "caregivers"
        const val BINDING_CODES_COLLECTION = "bindingCodes"
        const val ALERTS_COLLECTION = "alerts"
        const val CHECKINS_COLLECTION = "checkIns"
        const val DAILY_GOALS_COLLECTION = "dailyGoals"
        const val MEDITATION_EXERCISES_COLLECTION = "meditationExercises"
        const val COMPLETED_MEDITATIONS_COLLECTION = "completedMeditations"
        const val APPOINTMENTS_COLLECTION = "appointments"
        const val DIARY_ENTRIES_COLLECTION = "diaryEntries"
        const val STREAKS_COLLECTION = "streaks"
        const val ACHIEVEMENTS_COLLECTION = "achievements"
    }
}

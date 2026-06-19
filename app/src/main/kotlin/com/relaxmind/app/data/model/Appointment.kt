package com.relaxmind.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Appointment(
    val id: String = "",
    val patientId: String = "",
    val title: String = "",
    val type: String = "", // "cita" | "medicacion" | "recordatorio"
    val category: String = "", // custom tag (e.g., "psicólogo", "neurología")
    val date: String = "", // YYYY-MM-DD
    val time: String = "", // HH:mm
    val reminderTime: Int = 15, // minutes before
    val completed: Boolean = false,
    val notificationSent: Boolean = false,
    val notes: String = "",
    val recurring: Boolean = false,
    val recurringDays: List<Int> = emptyList(), // 1 = Monday, ..., 7 = Sunday
    @ServerTimestamp
    val createdAt: Date? = null
)

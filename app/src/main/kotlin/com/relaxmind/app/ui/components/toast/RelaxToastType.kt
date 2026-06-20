package com.relaxmind.app.ui.components.toast

enum class RelaxToastType {
    Success,
    Error,
    Warning,
    Info,
    SOS,
    CaregiverAlert,
    CheckInReminder,
    AppointmentReminder,
    MedicationReminder,
    Offline
}

data class RelaxToastData(
    val type: RelaxToastType,
    val title: String,
    val message: String,
    val durationMillis: Long = 3500L,
    val actionLabel: String? = null,
    val onActionClick: (() -> Unit)? = null
)

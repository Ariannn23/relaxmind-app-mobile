package com.relaxmind.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CaregiverAlert(
    val id: String = "",
    val caregiverId: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val severity: String = "info",
    val resolved: Boolean = false,
    val createdAtText: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)

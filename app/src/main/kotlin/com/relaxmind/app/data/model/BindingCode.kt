package com.relaxmind.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class BindingCode(
    val id: String = "",
    val code: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val patientLastName: String = "",
    val patientCondition: String = "",
    val patientAvatarUrl: String = "",
    val patientPhone: String = "",
    val caregiverId: String? = null,
    val expiresAt: Date? = null,
    @ServerTimestamp
    val createdAt: Date? = null
)

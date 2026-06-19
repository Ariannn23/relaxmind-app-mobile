package com.relaxmind.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CompletedMeditation(
    val id: String = "",
    val patientId: String = "",
    val exerciseId: String = "",
    val isGoalOfTheDay: Boolean = false,
    @ServerTimestamp
    val completedAt: Date? = null
)

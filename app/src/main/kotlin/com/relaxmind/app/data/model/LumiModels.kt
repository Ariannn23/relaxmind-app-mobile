package com.relaxmind.app.data.model

import com.google.firebase.firestore.DocumentId

data class LumiSession(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val isActive: Boolean = true,
    val title: String = "Chat con Lumi",
    val createdAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null
)

data class LumiMessage(
    @DocumentId val id: String = "",
    val role: String = "", // "user" or "model"
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

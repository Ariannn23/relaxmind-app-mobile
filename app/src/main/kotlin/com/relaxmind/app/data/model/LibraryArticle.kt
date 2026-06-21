package com.relaxmind.app.data.model

import java.util.Date

data class LibraryArticle(
    val id: String = "",
    val title: String = "",
    val summary: String = "",
    val content: String = "",
    val category: String = "",
    val targetRole: String = "", // "patient" | "caregiver" | "both"
    val coverImageUrl: String = "",
    val readTimeMinutes: Int = 5,
    val author: String? = null,
    val featured: Boolean = false,
    val createdAt: Date? = null
)

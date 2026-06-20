package com.relaxmind.app.data.model

data class NearbyHealthCenter(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val phoneNumber: String?,
    val distanceMeters: Int,
    val schedule: String?,
    val rating: Double?,
    val isPublic: Boolean = false, // Derived or mapped from Google Places types
    val typeName: String = "Centro de Salud" // Example: "Hospital", "Centro Comunitario", etc.
)

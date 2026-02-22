package com.example.kotlinproject.Model

data class HealthcareProfessional(
    val uid: String = "",
    val fullName: String = "",
    val role: String = "", // "doctor" or "pharmacist"
    val specialty: String = "",
    val schedule: Map<String, String> = emptyMap() // Day -> Time range
)
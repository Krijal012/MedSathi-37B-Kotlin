package com.example.kotlinproject.Model

data class Appointment(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val doctorSpecialty: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "Upcoming", // "Upcoming", "Completed", "Cancelled"
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
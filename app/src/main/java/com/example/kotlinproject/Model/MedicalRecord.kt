package com.example.kotlinproject.Model

data class MedicalRecord(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val date: String = "",
    val doctorName: String = "",
    val diagnosis: String = "",
    val prescription: List<String> = emptyList(),
    val billAmount: Double = 0.0,
    val status: String = "Paid",
    val createdAt: Long = System.currentTimeMillis()
)
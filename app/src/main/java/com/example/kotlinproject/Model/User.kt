package com.example.kotlinproject.Model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "", // "patient", "staff", "admin"
    val createdAt: Long = System.currentTimeMillis()
)
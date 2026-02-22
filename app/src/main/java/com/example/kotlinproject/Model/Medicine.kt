package com.example.kotlinproject.Model

data class Medicine(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val minStock: Int = 10,
    val description: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
package com.example.kotlinproject.Model

data class UserModel(
    val id: String = "",
    val email: String = ""
){
    fun toMap(): Map<String, Any>{
        return mapOf(
            "id" to id,
            "email" to email
        )
    }
}

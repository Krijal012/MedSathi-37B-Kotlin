package com.example.kotlinproject.Model

data class UserModel(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = ""
){
    fun toMap(): Map<String, Any>{
        return mapOf(
            "id" to id,
            "email" to email,
            "username" to username,
            "password" to password
        )
    }
}

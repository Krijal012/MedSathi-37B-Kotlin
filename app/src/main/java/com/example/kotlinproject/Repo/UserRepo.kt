package com.example.kotlinproject.Repo

import com.example.kotlinproject.Model.User

interface UserRepo {
    fun register(
        fullName: String,
        email: String,
        password: String,
        role: String,
        callback: (Boolean, String, User?) -> Unit
    )

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String, User?) -> Unit
    )

    fun logout()

    fun getCurrentUser(callback: (User?) -> Unit)

    fun isUserLoggedIn(): Boolean
}
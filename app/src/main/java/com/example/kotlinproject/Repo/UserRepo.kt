package com.example.kotlinproject.Repo

interface UserRepo {

    fun login(
        email: String,
        password: String,
        callBack: (Boolean, String) -> Unit
    )

    fun register(
        username: String,
        email: String,
        password: String,
        callBack: (Boolean, String, String) -> Unit
    )

    fun forgetPassword(
        email: String,
        callBack: (Boolean, String) -> Unit
    )
}

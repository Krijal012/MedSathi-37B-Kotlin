package com.example.kotlinproject.ViewModel

import androidx.lifecycle.ViewModel
import com.example.kotlinproject.Repo.UserRepo

class UserViewModel(private val repo: UserRepo) : ViewModel() {

    fun login(
        email: String,
        password: String,
        callBack: (Boolean, String) -> Unit
    ) {
        repo.login(email, password, callBack)
    }

    fun register(
        username: String,
        email: String,
        password: String,
        callBack: (Boolean, String, String) -> Unit
    ) {
        repo.register(username, email, password, callBack)
    }

    fun forgetPassword(
        email: String,
        callBack: (Boolean, String) -> Unit
    ) {
        repo.forgetPassword(email, callBack)
    }
}

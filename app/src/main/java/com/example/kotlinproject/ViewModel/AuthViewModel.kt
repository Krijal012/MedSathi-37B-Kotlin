package com.example.kotlinproject.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinproject.Model.User
import com.example.kotlinproject.Repo.UserRepo

class AuthViewModel(private val userRepo: UserRepo) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val message: String, val user: User?) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    fun register(fullName: String, email: String, password: String, role: String) {
        _authState.value = AuthState.Loading

        userRepo.register(fullName, email, password, role) { success, message, user ->
            if (success) {
                _authState.value = AuthState.Success(message, user)
            } else {
                _authState.value = AuthState.Error(message)
            }
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading

        userRepo.login(email, password) { success, message, user ->
            if (success) {
                _currentUser.value = user
                _authState.value = AuthState.Success(message, user)
            } else {
                _authState.value = AuthState.Error(message)
            }
        }
    }

    fun logout() {
        userRepo.logout()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun getCurrentUser() {
        userRepo.getCurrentUser { user ->
            _currentUser.value = user
        }
    }

    fun isUserLoggedIn(): Boolean {
        return userRepo.isUserLoggedIn()
    }
}
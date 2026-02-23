package com.example.kotlinproject.ViewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.kotlinproject.Model.User
import com.example.kotlinproject.Repo.UserRepo
import com.example.kotlinproject.Utils.CloudinaryConfig

class AuthViewModel(private val userRepo: UserRepo) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val message: String, val user: User? = null) : AuthState()
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

    fun updateProfile(context: Context, updatedUser: User, imageUri: Uri?) {
        _authState.value = AuthState.Loading

        if (imageUri == null) {
            saveProfile(updatedUser)
            return
        }

        try {
            MediaManager.get().upload(imageUri)
                .option("folder", "profiles")
                .option("unsigned", true)
                .option("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String ?: ""
                        saveProfile(updatedUser.copy(profilePhotoUrl = imageUrl))
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        _authState.postValue(AuthState.Error("Photo upload failed: ${error?.description}"))
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Cloudinary Error: ${e.message}")
        }
    }

    private fun saveProfile(user: User) {
        userRepo.updateProfile(user) { success, message ->
            if (success) {
                _currentUser.postValue(user)
                _authState.postValue(AuthState.Success(message, user))
            } else {
                _authState.postValue(AuthState.Error(message))
            }
        }
    }
}
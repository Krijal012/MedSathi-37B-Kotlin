package com.example.kotlinproject.Repo

import com.example.kotlinproject.Model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserRepoImpl : UserRepo {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun login(
        email: String,
        password: String,
        callBack: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callBack(true, "Login successful")
                } else {
                    callBack(false, task.exception?.message ?: "Login failed")
                }
            }
    }

    override fun register(
        username: String,
        email: String,
        password: String,
        callBack: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = UserModel(username, email)
                    val userId = auth.currentUser?.uid ?: ""
                    database.child(userId).setValue(user)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                callBack(true, "Registration successful", userId)
                            } else {
                                callBack(false, dbTask.exception?.message ?: "Database error", "")
                            }
                        }
                } else {
                    callBack(false, task.exception?.message ?: "Registration failed", "")
                }
            }
    }

    override fun forgetPassword(
        email: String,
        callBack: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callBack(true, "Reset email sent")
                } else {
                    callBack(false, task.exception?.message ?: "Failed to send email")
                }
            }
    }
}

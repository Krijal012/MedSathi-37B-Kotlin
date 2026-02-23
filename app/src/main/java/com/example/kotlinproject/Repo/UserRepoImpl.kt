package com.example.kotlinproject.Repo

import com.example.kotlinproject.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepoImpl : UserRepo {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val professionalsCollection = firestore.collection("professionals")

    override fun register(
        fullName: String,
        email: String,
        password: String,
        role: String,
        callback: (Boolean, String, User?) -> Unit
    ) {
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            callback(false, "All fields are required", null)
            return
        }

        val validRoles = listOf("patient", "staff", "admin", "pharmacist")
        if (role.lowercase() !in validRoles) {
            callback(false, "Invalid role selected", null)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback(false, "Invalid email format", null)
            return
        }

        if (password.length < 6) {
            callback(false, "Password must be at least 6 characters", null)
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid ?: ""

                    val user = User(
                        uid = userId,
                        fullName = fullName,
                        email = email,
                        role = role.lowercase(),
                        createdAt = System.currentTimeMillis()
                    )

                    usersCollection.document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            if (user.role == "pharmacist" || user.role == "staff") {
                                val professionalData = mapOf(
                                    "uid" to userId,
                                    "fullName" to fullName,
                                    "role" to user.role,
                                    "specialty" to if (user.role == "pharmacist") "Pharmacist" else "General Medicine",
                                    "schedule" to emptyMap<String, String>()
                                )
                                professionalsCollection.document(userId).set(professionalData)
                            }
                            callback(true, "Registration successful!", user)
                        }
                        .addOnFailureListener { e ->
                            firebaseUser?.delete()?.addOnCompleteListener {
                                callback(false, "Database error: ${e.message}", null)
                            }
                        }
                } else {
                    callback(false, "Auth failed: ${task.exception?.message}", null)
                }
            }
    }

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String, User?) -> Unit
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            callback(false, "Please fill all fields", null)
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""

                    usersCollection.document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val user = document.toObject(User::class.java)
                                callback(true, "Login successful", user)
                            } else {
                                callback(false, "User data not found", null)
                            }
                        }
                        .addOnFailureListener { e ->
                            callback(false, "Database error: ${e.message}", null)
                        }
                } else {
                    callback(false, "Login failed: ${task.exception?.message}", null)
                }
            }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUser(callback: (User?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(null)
            return
        }

        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.toObject(User::class.java))
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    override fun updateProfile(user: User, callback: (Boolean, String) -> Unit) {
        usersCollection.document(user.uid).set(user)
            .addOnSuccessListener {
                callback(true, "Profile updated successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update profile: ${e.message}")
            }
    }
}
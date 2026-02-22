package com.example.kotlinproject.Repo

import com.example.kotlinproject.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepoImpl : UserRepo {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    override fun register(
        fullName: String,
        email: String,
        password: String,
        role: String,
        callback: (Boolean, String, User?) -> Unit
    ) {
        // Basic validation
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

        // Create Firebase Auth account
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

                    // Save to Firestore
                    usersCollection.document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            callback(true, "Registration successful!", user)
                        }
                        .addOnFailureListener { e ->
                            // CRITICAL: If Firestore fails, delete the Auth user so they can retry
                            firebaseUser?.delete()?.addOnCompleteListener {
                                callback(false, "Database error: ${e.message}. Please ensure Firestore is enabled in Firebase Console.", null)
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
                                callback(false, "User data not found in database", null)
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
}
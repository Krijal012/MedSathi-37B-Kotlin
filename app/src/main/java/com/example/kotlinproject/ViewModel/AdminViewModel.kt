package com.example.kotlinproject.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinproject.Model.User
import com.google.firebase.firestore.FirebaseFirestore

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _stats = MutableLiveData<Map<String, Int>>()
    val stats: LiveData<Map<String, Int>> = _stats

    fun fetchUsers() {
        db.collection("users")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.toObjects(User::class.java)
                    _users.value = list
                    
                    val statsMap = mutableMapOf<String, Int>()
                    statsMap["totalPatients"] = list.count { it.role == "patient" }
                    statsMap["totalStaff"] = list.count { it.role == "staff" || it.role == "pharmacist" }
                    _stats.value = statsMap
                }
            }
    }

    fun deleteUser(userId: String) {
        db.collection("users").document(userId).delete()
        db.collection("professionals").document(userId).delete()
    }

    fun updateUser(user: User) {
        db.collection("users").document(user.uid).set(user)
    }
}
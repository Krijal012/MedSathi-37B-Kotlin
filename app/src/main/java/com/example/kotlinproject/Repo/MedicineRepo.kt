package com.example.kotlinproject.Repo

import com.example.kotlinproject.Model.Medicine
import com.google.firebase.firestore.FirebaseFirestore

class MedicineRepo {
    private val db = FirebaseFirestore.getInstance()
    private val medicinesCollection = db.collection("medicines")

    fun addMedicine(medicine: Medicine, callback: (Boolean, String) -> Unit) {
        val id = medicinesCollection.document().id
        val newMedicine = medicine.copy(id = id)
        medicinesCollection.document(id).set(newMedicine)
            .addOnSuccessListener { callback(true, "Medicine added successfully") }
            .addOnFailureListener { e -> callback(false, "Failed to add medicine: ${e.message}") }
    }

    fun updateMedicine(medicine: Medicine, callback: (Boolean, String) -> Unit) {
        medicinesCollection.document(medicine.id).set(medicine)
            .addOnSuccessListener { callback(true, "Medicine updated successfully") }
            .addOnFailureListener { e -> callback(false, "Failed to update medicine: ${e.message}") }
    }

    fun deleteMedicine(medicineId: String, callback: (Boolean, String) -> Unit) {
        medicinesCollection.document(medicineId).delete()
            .addOnSuccessListener { callback(true, "Medicine deleted successfully") }
            .addOnFailureListener { e -> callback(false, "Failed to delete medicine: ${e.message}") }
    }

    fun getAllMedicines(callback: (List<Medicine>) -> Unit) {
        medicinesCollection.get()
            .addOnSuccessListener { snapshot ->
                val medicines = snapshot.toObjects(Medicine::class.java)
                callback(medicines)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    fun searchMedicines(query: String, callback: (List<Medicine>) -> Unit) {
        medicinesCollection.whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + "\uf8ff")
            .get()
            .addOnSuccessListener { snapshot ->
                val medicines = snapshot.toObjects(Medicine::class.java)
                callback(medicines)
            }
            .addOnFailureListener { callback(emptyList()) }
    }
}
package com.example.kotlinproject.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinproject.Model.Appointment
import com.example.kotlinproject.Model.HealthcareProfessional
import com.example.kotlinproject.Model.MedicalRecord
import com.google.firebase.firestore.FirebaseFirestore

class StaffViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    private val _stats = MutableLiveData<Map<String, Int>>()
    val stats: LiveData<Map<String, Int>> = _stats

    private val _professionals = MutableLiveData<List<HealthcareProfessional>>()
    val professionals: LiveData<List<HealthcareProfessional>> = _professionals

    private val _medicalRecords = MutableLiveData<List<MedicalRecord>>()
    val medicalRecords: LiveData<List<MedicalRecord>> = _medicalRecords

    fun fetchAllAppointments() {
        db.collection("appointments")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                val list = snapshot.toObjects(Appointment::class.java)
                _appointments.value = list
                
                // Calculate stats
                val statsMap = mutableMapOf<String, Int>()
                statsMap["total"] = list.size
                statsMap["upcoming"] = list.count { it.status == "Upcoming" }
                statsMap["completed"] = list.count { it.status == "Completed" }
                
                _stats.value = statsMap
            }
    }

    private val _doctorCount = MutableLiveData<Int>()
    val doctorCount: LiveData<Int> = _doctorCount

    fun fetchDoctorCount() {
        db.collection("professionals")
            .whereEqualTo("role", "doctor")
            .get()
            .addOnSuccessListener { snapshot ->
                _doctorCount.value = snapshot.size()
            }
    }

    fun fetchPharmacists() {
        db.collection("professionals")
            .whereEqualTo("role", "pharmacist")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _professionals.value = snapshot.toObjects(HealthcareProfessional::class.java)
                }
            }
    }

    fun fetchMedicalRecords() {
        db.collection("medical_records")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _medicalRecords.value = snapshot.toObjects(MedicalRecord::class.java)
                }
            }
    }

    fun updateAppointmentStatus(appointmentId: String, status: String) {
        db.collection("appointments").document(appointmentId)
            .update("status", status)
    }
    
    fun updateMedicalRecord(record: MedicalRecord) {
        if (record.id.isEmpty()) {
            val docRef = db.collection("medical_records").document()
            db.collection("medical_records").document(docRef.id).set(record.copy(id = docRef.id))
        } else {
            db.collection("medical_records").document(record.id).set(record)
        }
    }
}
package com.example.kotlinproject.Repo

import com.example.kotlinproject.Model.Appointment
import com.google.firebase.firestore.FirebaseFirestore

class AppointmentRepo {
    private val db = FirebaseFirestore.getInstance()
    private val appointmentsCollection = db.collection("appointments")

    fun bookAppointment(appointment: Appointment, callback: (Boolean, String) -> Unit) {
        val id = appointmentsCollection.document().id
        val newAppointment = appointment.copy(id = id)
        
        appointmentsCollection.document(id).set(newAppointment)
            .addOnSuccessListener {
                callback(true, "Appointment booked successfully!")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to book appointment: ${e.message}")
            }
    }

    fun getPatientAppointments(patientId: String, callback: (List<Appointment>) -> Unit) {
        appointmentsCollection.whereEqualTo("patientId", patientId)
            .get()
            .addOnSuccessListener { snapshot ->
                val appointments = snapshot.toObjects(Appointment::class.java)
                callback(appointments)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun updateAppointmentStatus(appointmentId: String, status: String, callback: (Boolean, String) -> Unit) {
        appointmentsCollection.document(appointmentId)
            .update("status", status)
            .addOnSuccessListener {
                callback(true, "Appointment status updated to $status")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update status: ${e.message}")
            }
    }
}
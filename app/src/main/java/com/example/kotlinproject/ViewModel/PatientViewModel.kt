package com.example.kotlinproject.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinproject.Model.Appointment
import com.example.kotlinproject.Model.HealthcareProfessional
import com.example.kotlinproject.Model.MedicalRecord
import com.example.kotlinproject.Repo.AppointmentRepo
import com.google.firebase.firestore.FirebaseFirestore

class PatientViewModel(private val appointmentRepo: AppointmentRepo) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _bookingState = MutableLiveData<BookingState>()
    val bookingState: LiveData<BookingState> = _bookingState

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    private val _professionals = MutableLiveData<List<HealthcareProfessional>>()
    val professionals: LiveData<List<HealthcareProfessional>> = _professionals

    private val _medicalRecords = MutableLiveData<List<MedicalRecord>>()
    val medicalRecords: LiveData<List<MedicalRecord>> = _medicalRecords

    // Temporary data for the booking flow
    var selectedDoctorName: String = ""
    var selectedDoctorSpecialty: String = ""
    var selectedDate: String = ""
    var selectedTime: String = ""
    var selectedDoctorSchedule: Map<String, String> = emptyMap()

    sealed class BookingState {
        object Idle : BookingState()
        object Loading : BookingState()
        data class Success(val message: String) : BookingState()
        data class Error(val message: String) : BookingState()
    }

    fun bookAppointment(patientId: String, patientName: String, reason: String) {
        _bookingState.value = BookingState.Loading
        
        val appointment = Appointment(
            patientId = patientId,
            patientName = patientName,
            doctorName = selectedDoctorName,
            doctorSpecialty = selectedDoctorSpecialty,
            date = selectedDate,
            time = selectedTime,
            reason = reason,
            status = "Upcoming"
        )

        appointmentRepo.bookAppointment(appointment) { success, message ->
            if (success) {
                _bookingState.value = BookingState.Success(message)
            } else {
                _bookingState.value = BookingState.Error(message)
            }
        }
    }

    fun fetchAppointments(patientId: String) {
        appointmentRepo.getPatientAppointments(patientId) { list ->
            _appointments.value = list
        }
    }

    fun cancelAppointment(appointmentId: String, patientId: String) {
        _bookingState.value = BookingState.Loading
        appointmentRepo.updateAppointmentStatus(appointmentId, "Cancelled") { success, message ->
            if (success) {
                fetchAppointments(patientId)
                _bookingState.value = BookingState.Success("Appointment Cancelled")
            } else {
                _bookingState.value = BookingState.Error(message)
            }
        }
    }

    fun fetchProfessionals() {
        db.collection("professionals")
            .whereEqualTo("role", "pharmacist")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects(HealthcareProfessional::class.java)
                _professionals.value = list
            }
            .addOnFailureListener {
                _professionals.value = emptyList()
            }
    }

    fun fetchMedicalHistory(patientId: String) {
        db.collection("medical_records")
            .whereEqualTo("patientId", patientId)
            .get()
            .addOnSuccessListener { snapshot ->
                val records = snapshot.toObjects(MedicalRecord::class.java)
                _medicalRecords.value = records
            }
            .addOnFailureListener {
                _medicalRecords.value = emptyList()
            }
    }
    
    fun resetBookingState() {
        _bookingState.value = BookingState.Idle
    }
}
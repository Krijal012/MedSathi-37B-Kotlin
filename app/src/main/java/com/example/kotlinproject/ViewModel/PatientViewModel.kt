package com.example.kotlinproject.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinproject.Model.Appointment
import com.example.kotlinproject.Repo.AppointmentRepo

class PatientViewModel(private val appointmentRepo: AppointmentRepo) : ViewModel() {

    private val _bookingState = MutableLiveData<BookingState>()
    val bookingState: LiveData<BookingState> = _bookingState

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    // Temporary data for the booking flow
    var selectedDoctorName: String = ""
    var selectedDoctorSpecialty: String = ""
    var selectedDate: String = ""
    var selectedTime: String = ""

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
    
    fun resetBookingState() {
        _bookingState.value = BookingState.Idle
    }
}
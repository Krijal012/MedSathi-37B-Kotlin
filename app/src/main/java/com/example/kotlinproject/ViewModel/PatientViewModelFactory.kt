package com.example.kotlinproject.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlinproject.Repo.AppointmentRepo

class PatientViewModelFactory(private val appointmentRepo: AppointmentRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PatientViewModel(appointmentRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
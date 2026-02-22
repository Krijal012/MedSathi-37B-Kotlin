package com.example.kotlinproject.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlinproject.Repo.AppointmentRepo
import com.example.kotlinproject.Repo.MedicineRepo

class PharmacistViewModelFactory(
    private val medicineRepo: MedicineRepo,
    private val appointmentRepo: AppointmentRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PharmacistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PharmacistViewModel(medicineRepo, appointmentRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
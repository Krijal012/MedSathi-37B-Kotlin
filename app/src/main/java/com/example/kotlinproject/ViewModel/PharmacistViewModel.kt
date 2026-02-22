package com.example.kotlinproject.ViewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.kotlinproject.Model.Appointment
import com.example.kotlinproject.Model.MedicalRecord
import com.example.kotlinproject.Model.Medicine
import com.example.kotlinproject.Repo.AppointmentRepo
import com.example.kotlinproject.Repo.MedicineRepo
import com.example.kotlinproject.Utils.CloudinaryConfig
import com.google.firebase.firestore.FirebaseFirestore

class PharmacistViewModel(
    private val medicineRepo: MedicineRepo,
    private val appointmentRepo: AppointmentRepo
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _medicines = MutableLiveData<List<Medicine>>()
    val medicines: LiveData<List<Medicine>> = _medicines

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    private val _medicalRecords = MutableLiveData<List<MedicalRecord>>()
    val medicalRecords: LiveData<List<MedicalRecord>> = _medicalRecords

    private val _operationState = MutableLiveData<OperationState>()
    val operationState: LiveData<OperationState> = _operationState

    sealed class OperationState {
        object Idle : OperationState()
        object Loading : OperationState()
        data class Success(val message: String) : OperationState()
        data class Error(val message: String) : OperationState()
    }

    fun uploadAndAddMedicine(context: Context, medicine: Medicine, imageUri: Uri?) {
        _operationState.value = OperationState.Loading

        if (imageUri == null) {
            addMedicine(medicine)
            return
        }

        try {
            MediaManager.get().upload(imageUri)
                .option("folder", "medicines")
                .option("unsigned", true)
                .option("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String ?: ""
                        addMedicine(medicine.copy(imageUrl = imageUrl))
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        _operationState.postValue(OperationState.Error("Image upload failed: ${error?.description}"))
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } catch (e: Exception) {
            _operationState.value = OperationState.Error("Cloudinary Error: ${e.message}")
        }
    }

    private fun addMedicine(medicine: Medicine) {
        medicineRepo.addMedicine(medicine) { success, message ->
            if (success) {
                _operationState.postValue(OperationState.Success(message))
                fetchAllMedicines()
            } else {
                _operationState.postValue(OperationState.Error(message))
            }
        }
    }

    fun updateMedicine(medicine: Medicine) {
        _operationState.value = OperationState.Loading
        medicineRepo.updateMedicine(medicine) { success, message ->
            if (success) {
                _operationState.value = OperationState.Success(message)
                fetchAllMedicines()
            } else {
                _operationState.value = OperationState.Error(message)
            }
        }
    }

    fun deleteMedicine(medicineId: String) {
        _operationState.value = OperationState.Loading
        medicineRepo.deleteMedicine(medicineId) { success, message ->
            if (success) {
                _operationState.value = OperationState.Success(message)
                fetchAllMedicines()
            } else {
                _operationState.value = OperationState.Error(message)
            }
        }
    }

    fun fetchAllMedicines() {
        medicineRepo.getAllMedicines { list ->
            _medicines.value = list
        }
    }

    fun searchMedicines(query: String) {
        medicineRepo.searchMedicines(query) { list ->
            _medicines.value = list
        }
    }

    fun fetchAllAppointments() {
        db.collection("appointments")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects(Appointment::class.java)
                _appointments.value = list
            }
    }

    fun updatePharmacistSchedule(userId: String, schedule: Map<String, String>) {
        _operationState.value = OperationState.Loading
        db.collection("professionals").document(userId)
            .update("schedule", schedule)
            .addOnSuccessListener {
                _operationState.value = OperationState.Success("Schedule updated successfully")
            }
            .addOnFailureListener {
                _operationState.value = OperationState.Error("Failed to update schedule")
            }
    }

    fun fetchMedicalHistory(patientId: String) {
        _operationState.value = OperationState.Loading
        db.collection("medical_records")
            .whereEqualTo("patientId", patientId)
            .get()
            .addOnSuccessListener { snapshot ->
                val records = snapshot.toObjects(MedicalRecord::class.java)
                _medicalRecords.value = records
                _operationState.value = OperationState.Idle
            }
            .addOnFailureListener { e ->
                _operationState.value = OperationState.Error("Failed to fetch history: ${e.message}")
            }
    }

    fun finalizeBillAndCreateRecord(record: MedicalRecord, appointmentId: String? = null) {
        _operationState.value = OperationState.Loading
        val recordId = db.collection("medical_records").document().id
        val finalRecord = record.copy(id = recordId)
        
        db.collection("medical_records").document(recordId).set(finalRecord)
            .addOnSuccessListener {
                if (appointmentId != null) {
                    updateAppointmentStatus(appointmentId, "Completed")
                } else {
                    _operationState.value = OperationState.Success("Bill finalized and record created")
                }
            }
            .addOnFailureListener { e ->
                _operationState.value = OperationState.Error("Failed to save record: ${e.message}")
            }
    }

    fun updateAppointmentStatus(appointmentId: String, status: String) {
        _operationState.value = OperationState.Loading
        appointmentRepo.updateAppointmentStatus(appointmentId, status) { success, message ->
            if (success) {
                _operationState.postValue(OperationState.Success("Appointment $status"))
                fetchAllAppointments() // Refresh list
            } else {
                _operationState.postValue(OperationState.Error(message))
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}
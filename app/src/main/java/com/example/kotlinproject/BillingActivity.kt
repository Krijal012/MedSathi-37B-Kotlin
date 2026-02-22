package com.example.kotlinproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinproject.Model.Appointment
import com.example.kotlinproject.Model.MedicalRecord
import com.example.kotlinproject.Model.Medicine
import com.example.kotlinproject.Repo.AppointmentRepo
import com.example.kotlinproject.Repo.MedicineRepo
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory
import com.example.kotlinproject.ViewModel.PharmacistViewModel
import com.example.kotlinproject.ViewModel.PharmacistViewModelFactory
import kotlinx.coroutines.launch

class BillingActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepoImpl())
    }
    
    private val pharmacistViewModel: PharmacistViewModel by viewModels {
        PharmacistViewModelFactory(MedicineRepo(), AppointmentRepo())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authViewModel.getCurrentUser()
        pharmacistViewModel.fetchAllMedicines()
        pharmacistViewModel.fetchAllAppointments()
        
        setContent {
            MaterialTheme {
                BillingScreen(authViewModel, pharmacistViewModel)
            }
        }
    }
}

data class BillItem(val medicine: Medicine, var qty: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(authViewModel: AuthViewModel, pharmacistViewModel: PharmacistViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)
    val teal = Color(0xFF26D0CE)
    val context = LocalContext.current

    val currentUser = authViewModel.currentUser.observeAsState()
    val medicines = pharmacistViewModel.medicines.observeAsState(emptyList())
    val appointments = pharmacistViewModel.appointments.observeAsState(emptyList())
    val medicalHistory = pharmacistViewModel.medicalRecords.observeAsState(emptyList())

    var patientSearch by remember { mutableStateOf("") }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    var medicineSearch by remember { mutableStateOf("") }
    var billItems by remember { mutableStateOf(listOf<BillItem>()) }
    var showPatientDropdown by remember { mutableStateOf(false) }
    var showMedicineDropdown by remember { mutableStateOf(false) }

    val subtotal = billItems.sumOf { it.qty * it.medicine.price }
    val tax = subtotal * 0.13
    val total = subtotal + tax

    val filteredAppointments = appointments.value.filter { 
        it.patientName.contains(patientSearch, ignoreCase = true) && it.status == "Upcoming"
    }

    val filteredMedicines = medicines.value.filter { 
        it.name.contains(medicineSearch, ignoreCase = true) && it.stock > 0 
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PharmacistDrawerContent(
                currentScreen = "Billing",
                pharmacistName = currentUser.value?.fullName ?: "Pharmacist",
                onClose = { scope.launch { drawerState.close() } },
                onLogout = {
                    authViewModel.logout()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    (context as? ComponentActivity)?.finish()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Billing & History", color = Color.White, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBlue)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(lightGray)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 1. Search Patient / Appointment
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Search Booked Patient", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box {
                            OutlinedTextField(
                                value = patientSearch,
                                onValueChange = { 
                                    patientSearch = it
                                    showPatientDropdown = it.isNotEmpty()
                                },
                                label = { Text("Patient Name") },
                                leadingIcon = { Icon(Icons.Default.PersonSearch, null, tint = teal) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = showPatientDropdown && filteredAppointments.isNotEmpty(),
                                onDismissRequest = { showPatientDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                filteredAppointments.forEach { appt ->
                                    DropdownMenuItem(
                                        text = { Text("${appt.patientName} (${appt.date} ${appt.time})") },
                                        onClick = {
                                            selectedAppointment = appt
                                            patientSearch = appt.patientName
                                            showPatientDropdown = false
                                            pharmacistViewModel.fetchMedicalHistory(appt.patientId)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Display Medical History if patient selected
                if (selectedAppointment != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Patient Medical History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = darkBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (medicalHistory.value.isEmpty()) {
                                Text("No previous records found", color = Color.Gray, fontSize = 12.sp)
                            } else {
                                medicalHistory.value.forEach { record ->
                                    Text("• ${record.date}: ${record.diagnosis}", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. Billing Section (Existing logic refined)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Create Bill", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedTextField(
                                value = medicineSearch,
                                onValueChange = { 
                                    medicineSearch = it
                                    showMedicineDropdown = it.isNotEmpty()
                                },
                                label = { Text("Add Medicine") },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = teal) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = showMedicineDropdown && filteredMedicines.isNotEmpty(),
                                onDismissRequest = { showMedicineDropdown = false }
                            ) {
                                filteredMedicines.forEach { med ->
                                    DropdownMenuItem(
                                        text = { Text("${med.name} - Rs ${med.price}") },
                                        onClick = {
                                            if (!billItems.any { it.medicine.id == med.id }) {
                                                billItems = billItems + BillItem(med, 1)
                                            }
                                            medicineSearch = ""; showMedicineDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Items list...
                        billItems.forEachIndexed { index, item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.medicine.name, modifier = Modifier.weight(1f))
                                IconButton(onClick = { 
                                    val newList = billItems.toMutableList()
                                    if (item.qty > 1) newList[index] = item.copy(qty = item.qty - 1)
                                    billItems = newList
                                }) { Icon(Icons.Default.Remove, null) }
                                Text("${item.qty}")
                                IconButton(onClick = {
                                    val newList = billItems.toMutableList()
                                    newList[index] = item.copy(qty = item.qty + 1)
                                    billItems = newList
                                }) { Icon(Icons.Default.Add, null) }
                                IconButton(onClick = { billItems = billItems.filterIndexed { i, _ -> i != index } }) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                                }
                            }
                        }

                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total:", fontWeight = FontWeight.Bold)
                            Text("Rs ${total.toInt()}", fontWeight = FontWeight.Bold, color = teal)
                        }

                        Button(
                            onClick = {
                                if (selectedAppointment != null && billItems.isNotEmpty()) {
                                    val record = MedicalRecord(
                                        patientId = selectedAppointment!!.patientId,
                                        patientName = selectedAppointment!!.patientName,
                                        date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                                        doctorName = selectedAppointment!!.doctorName,
                                        diagnosis = selectedAppointment!!.reason,
                                        prescription = billItems.map { it.medicine.name },
                                        billAmount = total
                                    )
                                    pharmacistViewModel.finalizeBillAndCreateRecord(record)
                                    // Stock update...
                                    billItems.forEach { pharmacistViewModel.updateMedicine(it.medicine.copy(stock = it.medicine.stock - it.qty)) }
                                    billItems = emptyList(); selectedAppointment = null; patientSearch = ""
                                } else {
                                    Toast.makeText(context, "Select a patient and add items", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = teal)
                        ) {
                            Text("Finalize & Save Record")
                        }
                    }
                }
            }
        }
    }
}

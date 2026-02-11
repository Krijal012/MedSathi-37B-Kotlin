package com.example.kotlinproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text2.input.delete
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.map
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Patient(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val age: String = "",
    val gender: String = "",
    val address: String = "",
    val bloodGroup: String = "",
    val emergencyContact: String = ""
)

class ManagePatientsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ManagePatientsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePatientsScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)
    val teal = Color(0xFF26D0CE)

    var patients by remember { mutableStateOf<List<Patient>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()

    // Helper to reload data
    val refreshData = {
        scope.launch {
            isLoading = true
            try {
                val snapshot = firestore.collection("patients").get().await()
                patients = snapshot.documents.map { doc ->
                    Patient(
                        id = doc.id,
                        fullName = doc.getString("fullName") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        age = doc.getString("age") ?: "",
                        gender = doc.getString("gender") ?: "",
                        address = doc.getString("address") ?: "",
                        bloodGroup = doc.getString("bloodGroup") ?: "",
                        emergencyContact = doc.getString("emergencyContact") ?: ""
                    )
                }
            } catch (e: Exception) { e.printStackTrace() }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshData() }

    val filteredPatients = patients.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
    }

    // Dialogs
    if (showAddDialog) {
        PatientFormDialog(
            title = "Add Patient",
            onDismiss = { showAddDialog = false },
            onConfirm = { patientData ->
                scope.launch {
                    firestore.collection("patients").add(patientData).await()
                    refreshData()
                    showAddDialog = false
                }
            }
        )
    }

    if (showEditDialog && selectedPatient != null) {
        PatientFormDialog(
            title = "Edit Patient",
            initialPatient = selectedPatient,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedData ->
                scope.launch {
                    firestore.collection("patients").document(selectedPatient!!.id).update(updatedData).await()
                    refreshData()
                    showEditDialog = false
                }
            }
        )
    }

    if (showDeleteDialog && selectedPatient != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Patient") },
            text = { Text("Are you sure you want to delete ${selectedPatient?.fullName}?") },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        firestore.collection("patients").document(selectedPatient!!.id).delete().await()
                        refreshData()
                        showDeleteDialog = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminDrawerContent(currentScreen = "ManagePatients") {
                scope.launch { drawerState.close() }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("MedSathi - Admin", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBlue, titleContentColor = Color.White)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }, containerColor = teal) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().background(lightGray).padding(paddingValues).padding(16.dp)
            ) {
                Text("Manage Patients", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filteredPatients.forEach { patient ->
                            PatientCard(
                                patient = patient,
                                onEdit = { selectedPatient = it; showEditDialog = true },
                                onDelete = { selectedPatient = it; showDeleteDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientCard(patient: Patient, onEdit: (Patient) -> Unit, onDelete: (Patient) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(patient.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(patient.email, fontSize = 12.sp, color = Color.Gray)
                Text("Phone: ${patient.phone}", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = { onEdit(patient) }) { Icon(Icons.Default.Edit, "Edit", tint = Color.Blue) }
            IconButton(onClick = { onDelete(patient) }) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFormDialog(
    title: String,
    initialPatient: Patient? = null,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Any>) -> Unit
) {
    var name by remember { mutableStateOf(initialPatient?.fullName ?: "") }
    var email by remember { mutableStateOf(initialPatient?.email ?: "") }
    var phone by remember { mutableStateOf(initialPatient?.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(mapOf("fullName" to name, "email" to email, "phone" to phone))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
package com.example.kotlinproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

    var patients by remember {
        mutableStateOf<List<Patient>>(
            listOf(
                Patient("1", "John Smith", "john@email.com", "9800000001", "32", "Male", "Kathmandu", "A+", "9800000010"),
                Patient("2", "Jane Doe", "jane@email.com", "9800000002", "28", "Female", "Lalitpur", "B+", "9800000011"),
                Patient("3", "Ram Prasad", "ram@email.com", "9800000003", "45", "Male", "Bhaktapur", "O+", "9800000012"),
                Patient("4", "Sita Kumari", "sita@email.com", "9800000004", "35", "Female", "Pokhara", "AB+", "9800000013"),
                Patient("5", "Hari Bahadur", "hari@email.com", "9800000005", "52", "Male", "Chitwan", "A-", "9800000014")
            )
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()

    val refreshData = {
        scope.launch {
            isLoading = true
            try {
                val snapshot = firestore.collection("patients").get().await()
                val firestorePatients = snapshot.documents.map { doc ->
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
                if (firestorePatients.isNotEmpty()) {
                    patients = firestorePatients
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshData() }

    val filteredPatients = patients.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
    }

    if (showAddDialog) {
        PatientFormDialog(
            title = "Add Patient",
            onDismiss = { showAddDialog = false },
            onConfirm = { patientData ->
                scope.launch {
                    try {
                        firestore.collection("patients").add(patientData).await()
                        refreshData()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
                    try {
                        firestore.collection("patients").document(selectedPatient!!.id).update(updatedData).await()
                        refreshData()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                firestore.collection("patients").document(selectedPatient!!.id).delete().await()
                                refreshData()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            showDeleteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
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
                    actions = {
                        Text(
                            text = "Welcome, Admin",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp).padding(end = 8.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = darkBlue,
                        titleContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }, containerColor = teal) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(lightGray)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text("Manage Patients", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Add, edit, or remove patients", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search patients...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${filteredPatients.size} patients found",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = teal)
                    }
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
                        Spacer(modifier = Modifier.height(80.dp))
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
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFE5F7F7),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF26D0CE), modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(patient.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(patient.email, fontSize = 12.sp, color = Color.Gray)
                Text("Phone: ${patient.phone}", fontSize = 12.sp, color = Color.Gray)
                if (patient.age.isNotBlank()) {
                    Text("Age: ${patient.age}  |  ${patient.gender}  |  ${patient.bloodGroup}", fontSize = 11.sp, color = Color(0xFF26D0CE))
                }
            }
            Column {
                IconButton(onClick = { onEdit(patient) }) {
                    Icon(Icons.Default.Edit, "Edit", tint = Color(0xFF3B82F6))
                }
                IconButton(onClick = { onDelete(patient) }) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444))
                }
            }
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
    var age by remember { mutableStateOf(initialPatient?.age ?: "") }
    var gender by remember { mutableStateOf(initialPatient?.gender ?: "") }
    var address by remember { mutableStateOf(initialPatient?.address ?: "") }
    var bloodGroup by remember { mutableStateOf(initialPatient?.bloodGroup ?: "") }
    var emergencyContact by remember { mutableStateOf(initialPatient?.emergencyContact ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bloodGroup, onValueChange = { bloodGroup = it }, label = { Text("Blood Group") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = emergencyContact, onValueChange = { emergencyContact = it }, label = { Text("Emergency Contact") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(mapOf(
                        "fullName" to name,
                        "email" to email,
                        "phone" to phone,
                        "age" to age,
                        "gender" to gender,
                        "bloodGroup" to bloodGroup,
                        "address" to address,
                        "emergencyContact" to emergencyContact
                    ))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26D0CE))
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
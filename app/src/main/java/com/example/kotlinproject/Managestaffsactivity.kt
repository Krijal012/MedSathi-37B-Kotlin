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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Staff(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val department: String = "",
    val specialization: String = "",
    val experience: String = "",
    val qualification: String = "",
    val joinDate: String = ""
)

class ManageStaffsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ManageStaffsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStaffsScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)
    val teal = Color(0xFF26D0CE)

    var staffs by remember {
        mutableStateOf<List<Staff>>(
            listOf(
                Staff("1", "Dr. Ram Shrestha", "ram@email.com", "9800000001", "Doctor", "Cardiology", "Cardiologist", "10 years", "MBBS MD", "Jan 2020"),
                Staff("2", "Sita Sharma", "sita@email.com", "9800000002", "Nurse", "General Ward", "", "5 years", "BSc Nursing", "Mar 2021"),
                Staff("3", "Hari Thapa", "hari@email.com", "9800000003", "Receptionist", "Front Desk", "", "2 years", "BBA", "Jun 2023"),
                Staff("4", "Sunita Karki", "sunita@email.com", "9800000004", "Pharmacist", "Pharmacy", "", "4 years", "B.Pharm", "Feb 2022"),
                Staff("5", "Dr. Anita Rai", "anita@email.com", "9800000005", "Doctor", "Pediatrics", "Pediatrician", "8 years", "MBBS", "May 2019")
            )
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedStaff by remember { mutableStateOf<Staff?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var filterRole by remember { mutableStateOf("All") }

    val firestore = FirebaseFirestore.getInstance()

    val refreshData = {
        scope.launch {
            isLoading = true
            try {
                val snapshot = firestore.collection("staffs").get().await()
                val firestoreStaffs = snapshot.documents.map { doc ->
                    Staff(
                        id = doc.id,
                        fullName = doc.getString("fullName") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        role = doc.getString("role") ?: "",
                        department = doc.getString("department") ?: "",
                        specialization = doc.getString("specialization") ?: "",
                        experience = doc.getString("experience") ?: "",
                        qualification = doc.getString("qualification") ?: "",
                        joinDate = doc.getString("joinDate") ?: ""
                    )
                }
                if (firestoreStaffs.isNotEmpty()) {
                    staffs = firestoreStaffs
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshData() }

    val filteredStaffs = staffs.filter {
        val matchesSearch = it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true) ||
                it.department.contains(searchQuery, ignoreCase = true)
        val matchesRole = filterRole == "All" || it.role == filterRole
        matchesSearch && matchesRole
    }

    if (showAddDialog) {
        AddStaffDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { staff ->
                scope.launch {
                    try {
                        val staffData = hashMapOf(
                            "fullName" to staff.fullName,
                            "email" to staff.email,
                            "phone" to staff.phone,
                            "role" to staff.role,
                            "department" to staff.department,
                            "specialization" to staff.specialization,
                            "experience" to staff.experience,
                            "qualification" to staff.qualification,
                            "joinDate" to staff.joinDate
                        )
                        firestore.collection("staffs").add(staffData).await()
                        refreshData()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    showAddDialog = false
                }
            }
        )
    }

    if (showEditDialog && selectedStaff != null) {
        EditStaffDialog(
            staff = selectedStaff!!,
            onDismiss = { showEditDialog = false; selectedStaff = null },
            onConfirm = { updatedStaff ->
                scope.launch {
                    try {
                        val staffData = hashMapOf(
                            "fullName" to updatedStaff.fullName,
                            "email" to updatedStaff.email,
                            "phone" to updatedStaff.phone,
                            "role" to updatedStaff.role,
                            "department" to updatedStaff.department,
                            "specialization" to updatedStaff.specialization,
                            "experience" to updatedStaff.experience,
                            "qualification" to updatedStaff.qualification,
                            "joinDate" to updatedStaff.joinDate
                        )
                        firestore.collection("staffs").document(updatedStaff.id).update(staffData as Map<String, Any>).await()
                        refreshData()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    showEditDialog = false
                    selectedStaff = null
                }
            }
        )
    }

    if (showDeleteDialog && selectedStaff != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; selectedStaff = null },
            title = { Text("Delete Staff") },
            text = { Text("Are you sure you want to remove ${selectedStaff?.fullName} from the system?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                firestore.collection("staffs").document(selectedStaff!!.id).delete().await()
                                refreshData()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            showDeleteDialog = false
                            selectedStaff = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; selectedStaff = null }) { Text("Cancel") }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminDrawerContent(currentScreen = "ManageStaffs") {
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
                    Icon(Icons.Default.Add, contentDescription = "Add Staff", tint = Color.White)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(lightGray)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Manage Staff", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = "Add, edit, or remove staff members", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search staff...", fontSize = 14.sp, color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = darkBlue,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.width(120.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(filterRole, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("All", "Doctor", "Nurse", "Receptionist", "Pharmacist").forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = { filterRole = role; expanded = false }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StaffStatCard("Total Staff", staffs.size.toString(), Icons.Default.People, teal, Modifier.weight(1f))
                    StaffStatCard("Doctors", staffs.count { it.role == "Doctor" }.toString(), Icons.Default.MedicalServices, Color(0xFF3B82F6), Modifier.weight(1f))
                    StaffStatCard("Nurses", staffs.count { it.role == "Nurse" }.toString(), Icons.Default.LocalHospital, Color(0xFFEC4899), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = teal)
                    }
                } else if (filteredStaffs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No staff members found", fontSize = 16.sp, color = Color.Gray)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        filteredStaffs.forEach { staff ->
                            StaffCard(
                                staff = staff,
                                onEdit = { selectedStaff = staff; showEditDialog = true },
                                onDelete = { selectedStaff = staff; showDeleteDialog = true }
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
fun StaffStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, fontSize = 11.sp, color = Color.Gray)
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StaffCard(staff: Staff, onEdit: () -> Unit, onDelete: () -> Unit) {
    val roleColor = when (staff.role) {
        "Doctor" -> Color(0xFF3B82F6)
        "Nurse" -> Color(0xFFEC4899)
        "Receptionist" -> Color(0xFF8B5CF6)
        "Pharmacist" -> Color(0xFF10B981)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(shape = RoundedCornerShape(12.dp), color = roleColor.copy(alpha = 0.1f), modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (staff.role) {
                                    "Doctor" -> Icons.Default.MedicalServices
                                    "Nurse" -> Icons.Default.LocalHospital
                                    "Receptionist" -> Icons.Default.Contacts
                                    "Pharmacist" -> Icons.Default.Medication
                                    else -> Icons.Default.Person
                                },
                                contentDescription = staff.role,
                                tint = roleColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = staff.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Surface(color = roleColor, shape = RoundedCornerShape(12.dp)) {
                            Text(
                                text = staff.role,
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Text(text = " ${staff.email}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Text(text = " ${staff.phone}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF3B82F6)) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444)) }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (staff.department.isNotBlank()) StaffInfoChip(Icons.Default.Work, staff.department)
                if (staff.specialization.isNotBlank()) StaffInfoChip(Icons.Default.School, staff.specialization)
                if (staff.experience.isNotBlank()) StaffInfoChip(Icons.Default.Timer, "${staff.experience} exp")
            }
        }
    }
}

@Composable
fun StaffInfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6B7280))
            Text(text = text, fontSize = 11.sp, color = Color(0xFF374151))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffDialog(onDismiss: () -> Unit, onConfirm: (Staff) -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Doctor") }
    var department by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }
    var joinDate by remember { mutableStateOf("") }
    val roleOptions = listOf("Doctor", "Nurse", "Receptionist", "Pharmacist", "Technician")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Staff", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                var roleExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        roleOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { role = option; roleExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = specialization, onValueChange = { specialization = it }, label = { Text("Specialization (Optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = experience, onValueChange = { experience = it }, label = { Text("Experience (e.g. 5 years)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = qualification, onValueChange = { qualification = it }, label = { Text("Qualification") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = joinDate, onValueChange = { joinDate = it }, label = { Text("Join Date (e.g. Jan 2024)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && email.isNotBlank()) {
                        onConfirm(Staff(fullName = fullName, email = email, phone = phone, role = role, department = department, specialization = specialization, experience = experience, qualification = qualification, joinDate = joinDate))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26D0CE))
            ) { Text("Add Staff") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStaffDialog(staff: Staff, onDismiss: () -> Unit, onConfirm: (Staff) -> Unit) {
    var fullName by remember { mutableStateOf(staff.fullName) }
    var email by remember { mutableStateOf(staff.email) }
    var phone by remember { mutableStateOf(staff.phone) }
    var role by remember { mutableStateOf(staff.role) }
    var department by remember { mutableStateOf(staff.department) }
    var specialization by remember { mutableStateOf(staff.specialization) }
    var experience by remember { mutableStateOf(staff.experience) }
    var qualification by remember { mutableStateOf(staff.qualification) }
    var joinDate by remember { mutableStateOf(staff.joinDate) }
    val roleOptions = listOf("Doctor", "Nurse", "Receptionist", "Pharmacist", "Technician")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Staff", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                var roleExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        roleOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { role = option; roleExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = specialization, onValueChange = { specialization = it }, label = { Text("Specialization (Optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = experience, onValueChange = { experience = it }, label = { Text("Experience (e.g. 5 years)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = qualification, onValueChange = { qualification = it }, label = { Text("Qualification") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = joinDate, onValueChange = { joinDate = it }, label = { Text("Join Date (e.g. Jan 2024)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && email.isNotBlank()) {
                        onConfirm(staff.copy(fullName = fullName, email = email, phone = phone, role = role, department = department, specialization = specialization, experience = experience, qualification = qualification, joinDate = joinDate))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
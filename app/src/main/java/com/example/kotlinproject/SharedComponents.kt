package com.example.kotlinproject

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kotlinproject.Model.User
import com.example.kotlinproject.ViewModel.AdminViewModel
import com.example.kotlinproject.ViewModel.AuthViewModel
import kotlinx.coroutines.launch

// --- PATIENT DRAWER ---
@Composable
fun PatientDrawerContent(
    currentScreen: String,
    patientName: String,
    profilePhotoUrl: String = "",
    onClose: () -> Unit,
    onLogout: () -> Unit
) {
    val darkBlue = Color(0xFF1E3A5F)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(darkBlue)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (profilePhotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = profilePhotoUrl,
                    contentDescription = "Profile",
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.AccountCircle, "Profile", tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(patientName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Patient", color = Color(0xFFB0BEC5), fontSize = 12.sp)
            }
        }

        HorizontalDivider(color = Color(0xFF2C5F8D))
        Spacer(modifier = Modifier.height(16.dp))

        PatientDrawerMenuItem("Dashboard", Icons.Default.Home, currentScreen == "Dashboard") {
            onClose(); if (currentScreen != "Dashboard") context.startActivity(Intent(context, PatientDashboard::class.java))
        }
        PatientDrawerMenuItem("Profile", Icons.Default.Person, currentScreen == "Profile") {
            onClose(); if (currentScreen != "Profile") context.startActivity(Intent(context, ProfileActivity::class.java))
        }
        PatientDrawerMenuItem("Book Appointment", Icons.Default.DateRange, currentScreen == "BookAppointment") {
            onClose(); if (currentScreen != "BookAppointment") context.startActivity(Intent(context, BookAppointmentActivity::class.java))
        }
        PatientDrawerMenuItem("My Appointments", Icons.AutoMirrored.Filled.List, currentScreen == "MyAppointments") {
            onClose(); if (currentScreen != "MyAppointments") context.startActivity(Intent(context, MyAppointmentsActivity::class.java))
        }
        PatientDrawerMenuItem("Medical History", Icons.Default.Folder, currentScreen == "MedicalHistory") {
            onClose(); if (currentScreen != "MedicalHistory") context.startActivity(Intent(context, MedicalHistoryActivity::class.java))
        }
        PatientDrawerMenuItem("Doctor Availability", Icons.Default.Person, currentScreen == "DoctorAvailability") {
            onClose(); if (currentScreen != "DoctorAvailability") context.startActivity(Intent(context, DoctorAvailabilityActivity::class.java))
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout"); Spacer(modifier = Modifier.width(8.dp)); Text("Logout")
        }
    }
}

@Composable
fun PatientDrawerMenuItem(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF2C5F8D) else Color.Transparent
    Row(
        modifier = Modifier.fillMaxWidth().background(backgroundColor, RoundedCornerShape(8.dp)).clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp)); Text(title, color = Color.White, fontSize = 14.sp)
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// --- PHARMACIST DRAWER ---
@Composable
fun PharmacistDrawerContent(
    currentScreen: String,
    pharmacistName: String,
    onClose: () -> Unit,
    onLogout: () -> Unit
) {
    val darkBlue = Color(0xFF1E3A5F)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(darkBlue)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AccountCircle, "Profile", tint = Color.White, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(pharmacistName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Pharmacist", color = Color(0xFFB0BEC5), fontSize = 12.sp)
            }
        }

        HorizontalDivider(color = Color(0xFF2C5F8D))
        Spacer(modifier = Modifier.height(16.dp))

        PharmacistDrawerMenuItem("Dashboard", Icons.Default.Home, currentScreen == "Dashboard") {
            onClose(); if (currentScreen != "Dashboard") context.startActivity(Intent(context, PharmacistDashboard::class.java))
        }
        PharmacistDrawerMenuItem("Add Medicine", Icons.Default.Add, currentScreen == "AddMedicine") {
            onClose(); if (currentScreen != "AddMedicine") context.startActivity(Intent(context, AddMedicineActivity::class.java))
        }
        PharmacistDrawerMenuItem("Search Medicine", Icons.Default.Search, currentScreen == "SearchMedicine") {
            onClose(); if (currentScreen != "SearchMedicine") context.startActivity(Intent(context, SearchMedicineActivity::class.java))
        }
        PharmacistDrawerMenuItem("Billing", Icons.Default.Receipt, currentScreen == "Billing") {
            onClose(); if (currentScreen != "Billing") context.startActivity(Intent(context, BillingActivity::class.java))
        }
        PharmacistDrawerMenuItem("My Schedule", Icons.Default.Schedule, currentScreen == "Schedule") {
            onClose(); if (currentScreen != "Schedule") context.startActivity(Intent(context, PharmacistScheduleActivity::class.java))
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout"); Spacer(modifier = Modifier.width(8.dp)); Text("Logout")
        }
    }
}

@Composable
fun PharmacistDrawerMenuItem(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF2C5F8D) else Color.Transparent
    Row(
        modifier = Modifier.fillMaxWidth().background(backgroundColor, RoundedCornerShape(8.dp)).clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp)); Text(title, color = Color.White, fontSize = 14.sp)
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// --- ADMIN COMPONENTS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    title: String,
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel,
    roles: List<String>
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentUser = authViewModel.currentUser.observeAsState()
    val allUsers = adminViewModel.users.observeAsState(emptyList())
    
    val filteredUsers = allUsers.value.filter { it.role in roles }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminDrawerContent(
                currentScreen = if (roles.contains("patient") && roles.size == 1) "ManagePatients" else "ManageStaffs",
                adminName = currentUser.value?.fullName ?: "Admin",
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
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E3A5F),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                if (filteredUsers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No users found", color = Color.Gray)
                    }
                } else {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filteredUsers.forEach { user ->
                            UserCard(user, onDelete = { adminViewModel.deleteUser(user.uid) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(user.email, fontSize = 12.sp, color = Color.Gray)
                Text("Role: ${user.role.replaceFirstChar { it.uppercase() }}", fontSize = 12.sp, color = Color(0xFF26D0CE))
            }
            Row {
                IconButton(onClick = { /* Edit logic */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Blue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun AdminDrawerContent(
    currentScreen: String = "Dashboard",
    adminName: String,
    onClose: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val darkBlue = Color(0xFF1E3A5F)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(darkBlue)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // User Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = adminName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Administrator",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp
                )
            }
        }

        HorizontalDivider(color = Color(0xFF2C5F8D))
        Spacer(modifier = Modifier.height(16.dp))

        AdminDrawerMenuItem("Dashboard", Icons.Default.Dashboard, currentScreen == "Dashboard") {
            onClose(); if (currentScreen != "Dashboard") context.startActivity(Intent(context, AdminDashboard::class.java))
        }
        AdminDrawerMenuItem("Manage Patients", Icons.Default.People, currentScreen == "ManagePatients") {
            onClose(); if (currentScreen != "ManagePatients") context.startActivity(Intent(context, Managepatientactivity::class.java))
        }
        AdminDrawerMenuItem("Manage Staffs", Icons.Default.Badge, currentScreen == "ManageStaffs") {
            onClose(); if (currentScreen != "ManageStaffs") context.startActivity(Intent(context, ManageStaffsActivity::class.java))
        }

        // Logout Button
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }
}

@Composable
fun AdminDrawerMenuItem(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF2C5F8D) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = Color.White, fontSize = 14.sp)
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// --- SHARED UI COMPONENTS ---
@Composable
fun AppointmentStepper(currentStep: Int) {
    val teal = Color(0xFF26D0CE)
    val gray = Color(0xFFD1D5DB)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepIndicator("1", currentStep >= 1, currentStep > 1, Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.weight(1f).height(2.dp), color = if (currentStep > 1) teal else gray)
        StepIndicator("2", currentStep >= 2, currentStep > 2, Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.weight(1f).height(2.dp), color = if (currentStep > 2) teal else gray)
        StepIndicator("3", currentStep >= 3, currentStep > 3, Modifier.weight(1f))
    }
}

@Composable
fun StepIndicator(stepNumber: String, isActive: Boolean, isCompleted: Boolean, modifier: Modifier = Modifier) {
    val teal = Color(0xFF26D0CE); val gray = Color(0xFFD1D5DB)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.size(50.dp).clip(CircleShape).background(if (isActive || isCompleted) teal else gray),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) Icon(Icons.Default.Check, "Completed", tint = Color.White, modifier = Modifier.size(28.dp))
            else Text(stepNumber, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TimeSlotChip(time: String, isSelected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor = if (isSelected) Color(0xFF26D0CE) else Color(0xFFF3F4F6)
    val textColor = if (isSelected) Color.White else Color.Black
    Box(
        modifier = modifier.height(40.dp).background(backgroundColor, RoundedCornerShape(8.dp)).clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = time, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DoctorCard(doctorName: String, specialty: String, isSelected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val teal = Color(0xFF26D0CE); val borderColor = if (isSelected) teal else Color(0xFFE5E7EB)
    Card(
        modifier = modifier.height(80.dp).border(2.dp, borderColor, RoundedCornerShape(8.dp)).clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MedicalServices, "Doctor", tint = teal, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(doctorName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(specialty, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

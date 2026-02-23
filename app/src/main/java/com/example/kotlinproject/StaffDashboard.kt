package com.example.kotlinproject

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory
import com.example.kotlinproject.ViewModel.StaffViewModel
import kotlinx.coroutines.launch

class StaffDashboard : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepoImpl())
    }
    
    private val staffViewModel: StaffViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authViewModel.getCurrentUser()
        staffViewModel.fetchAllAppointments()
        staffViewModel.fetchDoctorCount()

        setContent {
            MaterialTheme {
                StaffDashboardScreen(authViewModel, staffViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(authViewModel: AuthViewModel, staffViewModel: StaffViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)
    val context = LocalContext.current
    
    val currentUser = authViewModel.currentUser.observeAsState()
    val appointments = staffViewModel.appointments.observeAsState(emptyList())
    val stats = staffViewModel.stats.observeAsState(emptyMap())
    val doctorCount = staffViewModel.doctorCount.observeAsState(0)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StaffDrawerContent(
                currentScreen = "Dashboard",
                staffName = currentUser.value?.fullName ?: "Staff",
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
                    title = {
                        Text(
                            "MedSathi - Staff",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        Text(
                            text = currentUser.value?.fullName ?: "Staff",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 8.dp)
                                .clickable {
                                    context.startActivity(Intent(context, ProfileActivity::class.java))
                                }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = darkBlue,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(lightGray)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Staff Dashboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Manage patient flow and doctor schedules",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                StaffStatsGrid(
                    totalQueue = stats.value["total"] ?: 0,
                    inQueue = stats.value["upcoming"] ?: 0,
                    completed = stats.value["completed"] ?: 0,
                    doctorsAvailable = doctorCount.value
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Current Queue",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (appointments.value.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No appointments found", color = Color.Gray)
                    }
                } else {
                    appointments.value.take(10).forEachIndexed { index, appt ->
                        StaffQueueCard(
                            queueNumber = String.format("%03d", index + 1),
                            patientName = appt.patientName,
                            doctorName = appt.doctorName,
                            status = appt.status,
                            time = appt.time
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StaffDrawerContent(
    currentScreen: String,
    staffName: String,
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
                    text = staffName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Staff Member",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp
                )
            }
        }

        HorizontalDivider(color = Color(0xFF2C5F8D))
        Spacer(modifier = Modifier.height(16.dp))

        StaffDrawerMenuItem("Dashboard", Icons.Default.Dashboard, currentScreen == "Dashboard") {
            onClose(); if (currentScreen != "Dashboard") context.startActivity(Intent(context, StaffDashboard::class.java))
        }
        StaffDrawerMenuItem("Doctor Schedule", Icons.Default.CalendarToday, currentScreen == "DoctorSchedule") {
            onClose(); if (currentScreen != "DoctorSchedule") context.startActivity(Intent(context, DoctorScheduleActivity::class.java))
        }
        StaffDrawerMenuItem("Patient Records", Icons.Default.FolderShared, currentScreen == "PatientRecords") {
            onClose(); if (currentScreen != "PatientRecords") context.startActivity(Intent(context, PatientRecordsActivity::class.java))
        }
        StaffDrawerMenuItem("Patient Queue", Icons.Default.Queue, currentScreen == "PatientQueue") {
            onClose(); if (currentScreen != "PatientQueue") context.startActivity(Intent(context, PatientQueueActivity::class.java))
        }

        // Logout Button
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }
}

@Composable
fun StaffDrawerMenuItem(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
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

@Composable
fun StaffStatsGrid(totalQueue: Int, inQueue: Int, completed: Int, doctorsAvailable: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StaffStatCard("Patients Queue", totalQueue.toString(), Icons.Default.People, Modifier.weight(1f))
        StaffStatCard("In Queue", inQueue.toString(), Icons.Default.AccessTime, Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StaffStatCard("Completed", completed.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
        StaffStatCard("Doctors Available", doctorsAvailable.toString(), Icons.Default.MedicalServices, Modifier.weight(1f))
    }
}

@Composable
fun StaffStatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = when (title) {
                        "Patients Queue" -> Color(0xFF6B7280)
                        "In Queue" -> Color(0xFF84CC16)
                        "Completed" -> Color(0xFF26D0CE)
                        else -> Color(0xFF3B82F6)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun StaffQueueCard(
    queueNumber: String,
    patientName: String,
    doctorName: String,
    status: String,
    time: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = queueNumber,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280),
                modifier = Modifier.width(60.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patientName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(text = doctorName, fontSize = 13.sp, color = Color(0xFF26D0CE))
            }
            Column(horizontalAlignment = Alignment.End) {
                val statusColor = when(status) {
                    "Completed" -> Color(0xFF26D0CE)
                    "Upcoming" -> Color(0xFF84CC16)
                    "Cancelled" -> Color(0xFFE53E3E)
                    else -> Color(0xFF6B7280)
                }
                Surface(color = statusColor, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = time, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
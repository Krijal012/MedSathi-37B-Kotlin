package com.example.kotlinproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class StaffDashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                StaffDashboardScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StaffDrawerContent(currentScreen = "Dashboard") {
                scope.launch { drawerState.close() }
            }
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
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        Text(
                            text = "Welcome back, {staff name}",
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

                // Title
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

                // Stats Grid
                StaffStatsGrid()

                Spacer(modifier = Modifier.height(20.dp))

                // Current Queue Section
                Text(
                    text = "Current Queue",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Queue Items
                repeat(4) {
                    StaffQueueCard(
                        queueNumber = "001",
                        patientName = "John Smith",
                        doctorName = "Dr. Ram Shrestha",
                        status = "In Progress",
                        time = "5 Mins"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StaffDrawerContent(currentScreen: String, onClose: () -> Unit) {
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

        Text(
            text = "MedSathi -",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Staff",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        StaffDrawerMenuItem("Dashboard", Icons.Default.Home, currentScreen == "Dashboard") {
            onClose()
        }
        StaffDrawerMenuItem("Doctor Schedule", Icons.Default.CalendarMonth, currentScreen == "DoctorSchedule") {
            onClose()
            context.startActivity(Intent(context, DoctorScheduleActivity::class.java))
        }
        StaffDrawerMenuItem("Patient Records", Icons.Default.FolderOpen, currentScreen == "PatientRecords") {
            onClose()
            context.startActivity(Intent(context, PatientRecordsActivity::class.java))
        }
        StaffDrawerMenuItem("Patient Queue", Icons.Default.People, currentScreen == "PatientQueue") {
            onClose()
            context.startActivity(Intent(context, PatientQueueActivity::class.java))
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
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun StaffStatsGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StaffStatCard(
            "Patients Queue",
            "20",
            Icons.Default.People,
            Modifier.weight(1f)
        )
        StaffStatCard(
            "In Queue",
            "8",
            Icons.Default.AccessTime,
            Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StaffStatCard(
            "Completed",
            "12",
            Icons.Default.CheckCircle,
            Modifier.weight(1f)
        )
        StaffStatCard(
            "Doctors Available",
            "15",
            Icons.Default.MedicalServices,
            Modifier.weight(1f)
        )
    }
}

@Composable
fun StaffStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
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
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Normal
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
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
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
            // Queue Number
            Text(
                text = queueNumber,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280),
                modifier = Modifier.width(70.dp)
            )

            // Patient Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = doctorName,
                    fontSize = 14.sp,
                    color = Color(0xFF26D0CE)
                )
            }

            // Status Badge
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = Color(0xFF6B7280),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
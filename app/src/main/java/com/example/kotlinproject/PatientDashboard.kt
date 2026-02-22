package com.example.kotlinproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import com.example.kotlinproject.Repo.AppointmentRepo
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory
import com.example.kotlinproject.ViewModel.PatientViewModel
import com.example.kotlinproject.ViewModel.PatientViewModelFactory
import kotlinx.coroutines.launch

class PatientDashboard : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepoImpl())
    }
    
    private val patientViewModel: PatientViewModel by viewModels {
        PatientViewModelFactory(AppointmentRepo())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authViewModel.getCurrentUser()
        
        authViewModel.currentUser.observe(this) { user ->
            if (user != null) {
                patientViewModel.fetchAppointments(user.uid)
            }
        }

        setContent {
            MaterialTheme {
                PatientDashboardScreen(authViewModel, patientViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(authViewModel: AuthViewModel, patientViewModel: PatientViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val context = LocalContext.current
    
    val currentUser = authViewModel.currentUser.observeAsState()
    val appointments = patientViewModel.appointments.observeAsState(emptyList())
    
    val upcomingAppointments = appointments.value.filter { it.status == "Upcoming" }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PatientDrawerContent(
                currentScreen = "Dashboard",
                patientName = currentUser.value?.fullName ?: "Patient",
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
                    title = { Text("MedSathi") },
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
                    .background(lightGray)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                WelcomeHeader(patientName = currentUser.value?.fullName ?: "Patient")
                Spacer(modifier = Modifier.height(16.dp))
                StatsGrid(upcomingCount = upcomingAppointments.size)
                Spacer(modifier = Modifier.height(16.dp))
                UpcomingAppointmentsCard(upcomingAppointments)
                Spacer(modifier = Modifier.height(16.dp))
                HealthMetricsCard()
            }
        }
    }
}

@Composable
fun WelcomeHeader(patientName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Welcome back,", fontSize = 14.sp, color = Color.Gray)
                Text(
                    patientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A5F)
                )
            }
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF1E3A5F)
            )
        }
    }
}

@Composable
fun StatsGrid(upcomingCount: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Upcoming\nAppointments", upcomingCount.toString(), Icons.Default.DateRange, Modifier.weight(1f))
            StatCard("Prescriptions\nActive", "3", Icons.Default.LocalPharmacy, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Medical\nRecords", "12", Icons.Default.Article, Modifier.weight(1f))
            StatCard("Health\nScore", "82%", Icons.Default.FavoriteBorder, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    title,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    icon,
                    contentDescription = title,
                    tint = Color(0xFF26D0CE),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun UpcomingAppointmentsCard(upcoming: List<com.example.kotlinproject.Model.Appointment>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Upcoming Appointments", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            if (upcoming.isEmpty()) {
                Text("No upcoming appointments", color = Color.Gray, fontSize = 13.sp)
            } else {
                upcoming.take(3).forEach { appointment ->
                    AppointmentItem(appointment)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(appointment: com.example.kotlinproject.Model.Appointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.LocalHospital,
                    contentDescription = "Doctor",
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF26D0CE)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(appointment.doctorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(appointment.doctorSpecialty, fontSize = 11.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(appointment.date, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(appointment.time, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun HealthMetricsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Health Metrics", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            HealthMetricItem("Blood Pressure", "120/80", "Normal")
            Spacer(modifier = Modifier.height(8.dp))
            HealthMetricItem("Heart Rate", "72 BPM", "Normal")
            Spacer(modifier = Modifier.height(8.dp))
            HealthMetricItem("Weight", "70 KG", "Normal")
        }
    }
}

@Composable
fun HealthMetricItem(title: String, value: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = title,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF26D0CE)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, fontSize = 11.sp, color = Color.Gray)
                    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                status,
                fontSize = 11.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
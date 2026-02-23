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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory
import com.example.kotlinproject.ViewModel.StaffViewModel
import kotlinx.coroutines.launch

class PatientQueueActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepoImpl())
    }
    private val staffViewModel: StaffViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        authViewModel.getCurrentUser()
        staffViewModel.fetchAllAppointments()
        
        setContent {
            MaterialTheme {
                PatientQueueScreen(authViewModel, staffViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientQueueScreen(authViewModel: AuthViewModel, staffViewModel: StaffViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)
    val context = LocalContext.current
    
    val currentUser = authViewModel.currentUser.observeAsState()
    val appointments = staffViewModel.appointments.observeAsState(emptyList())
    
    // Filter for upcoming (waiting) patients
    val queue = appointments.value.filter { it.status == "Upcoming" }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StaffDrawerContent(
                currentScreen = "PatientQueue",
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
                    title = { Text("Patient Queue") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = darkBlue,
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
                Text(text = "Patients Waiting", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = "Current queue of patients waiting for consultation", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                if (queue.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Queue is currently empty", color = Color.Gray)
                    }
                } else {
                    queue.forEachIndexed { index, appt ->
                        PatientQueueCard(
                            queueNumber = String.format("%03d", index + 1),
                            patientName = appt.patientName,
                            doctorName = appt.doctorName,
                            time = appt.time,
                            status = appt.status,
                            onComplete = {
                                staffViewModel.updateAppointmentStatus(appt.id, "Completed")
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PatientQueueCard(
    queueNumber: String,
    patientName: String,
    doctorName: String,
    time: String,
    status: String,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFE0F2F1),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = queueNumber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00695C)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = patientName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = doctorName, fontSize = 13.sp, color = Color(0xFF1E3A5F))
                Text(text = "Scheduled: $time", fontSize = 11.sp, color = Color.Gray)
            }

            IconButton(onClick = onComplete) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Mark as Completed",
                    tint = Color(0xFF26D0CE),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
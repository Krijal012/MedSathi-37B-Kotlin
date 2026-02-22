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
import com.example.kotlinproject.Repo.AppointmentRepo
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory
import com.example.kotlinproject.ViewModel.PatientViewModel
import com.example.kotlinproject.ViewModel.PatientViewModelFactory
import kotlinx.coroutines.launch

class DoctorAvailabilityActivity : ComponentActivity() {
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
        patientViewModel.fetchProfessionals()
        setContent {
            MaterialTheme {
                DoctorAvailabilityScreen(authViewModel, patientViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAvailabilityScreen(authViewModel: AuthViewModel, patientViewModel: PatientViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkBlue = Color(0xFF1E3A5F)
    val lightGray = Color(0xFFF5F5F5)
    val teal = Color(0xFF26D0CE)

    val currentUser = authViewModel.currentUser.observeAsState()
    val professionals = patientViewModel.professionals.observeAsState(emptyList())
    
    var searchQuery by remember { mutableStateOf("") }

    val filteredProfessionals = professionals.value.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) || it.role.contains(searchQuery, ignoreCase = true)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PatientDrawerContent(
                currentScreen = "DoctorAvailability",
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
                    title = { Text("Availability", color = Color.White, fontSize = 18.sp) },
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
                    .padding(24.dp)
            ) {
                Text(text = "Healthcare Professionals", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = "View availability of Doctors and Pharmacists", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or role...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (filteredProfessionals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No professionals found", color = Color.Gray)
                    }
                } else {
                    filteredProfessionals.forEach { prof ->
                        ProfessionalCard(
                            name = prof.fullName,
                            role = prof.role.replaceFirstChar { it.uppercase() },
                            specialty = prof.specialty,
                            schedule = prof.schedule
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessionalCard(
    name: String,
    role: String,
    specialty: String,
    schedule: Map<String, String>
) {
    val teal = Color(0xFF26D0CE)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (role.lowercase() == "pharmacist") Icons.Default.Medication else Icons.Default.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = teal
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$role | $specialty", fontSize = 13.sp, color = Color.Gray)
                }
            }

            if (schedule.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Working Hours:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                schedule.forEach { (day, time) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = day, fontSize = 11.sp, color = Color.Gray)
                        Text(text = time, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Schedule not set", fontSize = 11.sp, color = Color.LightGray)
            }
        }
    }
}

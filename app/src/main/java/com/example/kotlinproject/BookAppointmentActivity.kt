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

class BookAppointmentActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepoImpl())
    }
    
    companion object {
        var patientViewModel: PatientViewModel? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        if (patientViewModel == null) {
            patientViewModel = PatientViewModel(AppointmentRepo())
        }
        
        authViewModel.getCurrentUser()
        
        setContent {
            MaterialTheme {
                BookAppointmentScreen(authViewModel, patientViewModel!!)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(authViewModel: AuthViewModel, patientViewModel: PatientViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkBlue = Color(0xFF1E3A5F)
    val lightGray = Color(0xFFF5F5F5)
    val teal = Color(0xFF26D0CE)

    val currentUser = authViewModel.currentUser.observeAsState()
    var selectedDoctorIndex by remember { mutableStateOf(-1) }
    
    val doctors = listOf(
        Pair("Dr. Ram Shrestha", "Cardiologist"),
        Pair("Dr. Sita Sharma", "Neurologist"),
        Pair("Dr. Hari Thapa", "Orthopedic"),
        Pair("Dr. Anita Rai", "Pediatrician"),
        Pair("Dr. Sunil Karki", "Dermatologist"),
        Pair("Dr. Maya Singh", "General Physician")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PatientDrawerContent(
                currentScreen = "BookAppointment",
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
                    title = { Text("Book Appointment", color = Color.White, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = currentUser.value?.fullName ?: "Patient",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
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
                Text(text = "Step 1: Select Doctor", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                AppointmentStepper(currentStep = 1)

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        doctors.chunked(2).forEachIndexed { rowIndex, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEachIndexed { colIndex, doctor ->
                                    val index = rowIndex * 2 + colIndex
                                    DoctorCard(
                                        doctorName = doctor.first,
                                        specialty = doctor.second,
                                        isSelected = selectedDoctorIndex == index,
                                        onSelect = { 
                                            selectedDoctorIndex = index
                                            patientViewModel.selectedDoctorName = doctor.first
                                            patientViewModel.selectedDoctorSpecialty = doctor.second
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        context.startActivity(Intent(context, SelectDateTimeActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = teal),
                    enabled = selectedDoctorIndex != -1
                ) {
                    Text("Continue", fontSize = 16.sp)
                }
            }
        }
    }
}

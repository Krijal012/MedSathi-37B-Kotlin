package com.example.kotlinproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory
import com.example.kotlinproject.ViewModel.PatientViewModel
import com.example.kotlinproject.BookAppointmentActivity.Companion.patientViewModel
import kotlinx.coroutines.launch

class AppointmentDetailsActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepoImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authViewModel.getCurrentUser()
        setContent {
            MaterialTheme {
                AppointmentDetailsScreen(authViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailsScreen(authViewModel: AuthViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkBlue = Color(0xFF1E3A5F)
    val lightGray = Color(0xFFF5F5F5)
    val teal = Color(0xFF26D0CE)

    val currentUser = authViewModel.currentUser.observeAsState()
    val bookingState = patientViewModel?.bookingState?.observeAsState()
    var reasonForVisit by remember { mutableStateOf("") }

    LaunchedEffect(bookingState?.value) {
        when (val state = bookingState?.value) {
            is PatientViewModel.BookingState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                patientViewModel?.resetBookingState()
                val intent = Intent(context, PatientDashboard::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                (context as? ComponentActivity)?.finish()
            }
            is PatientViewModel.BookingState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

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
                    title = { Text("Appointment Details", color = Color.White, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        Text(
                            text = currentUser.value?.fullName ?: "Patient",
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
                Text(text = "Step 3: Confirm Booking", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                AppointmentStepper(currentStep = 3)

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Reason for visit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reasonForVisit,
                            onValueChange = { reasonForVisit = it },
                            placeholder = { Text("Describe your symptoms...") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(text = "Appointment Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SummaryRow("Doctor:", patientViewModel?.selectedDoctorName ?: "N/A")
                                Spacer(modifier = Modifier.height(8.dp))
                                SummaryRow("Specialty:", patientViewModel?.selectedDoctorSpecialty ?: "N/A")
                                Spacer(modifier = Modifier.height(8.dp))
                                SummaryRow("Date:", patientViewModel?.selectedDate ?: "N/A")
                                Spacer(modifier = Modifier.height(8.dp))
                                SummaryRow("Time:", patientViewModel?.selectedTime ?: "N/A")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { (context as? ComponentActivity)?.finish() },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Back")
                    }
                    Button(
                        onClick = {
                            val user = currentUser.value
                            if (user != null) {
                                patientViewModel?.bookAppointment(user.uid, user.fullName, reasonForVisit)
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = teal),
                        enabled = bookingState?.value !is PatientViewModel.BookingState.Loading
                    ) {
                        if (bookingState?.value is PatientViewModel.BookingState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Confirm Booking")
                        }
                    }
                }
            }
        }
    }
}

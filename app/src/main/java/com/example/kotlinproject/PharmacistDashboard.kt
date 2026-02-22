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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinproject.Model.Appointment
import com.example.kotlinproject.Repo.AppointmentRepo
import com.example.kotlinproject.Repo.MedicineRepo
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory
import com.example.kotlinproject.ViewModel.PharmacistViewModel
import com.example.kotlinproject.ViewModel.PharmacistViewModelFactory
import kotlinx.coroutines.launch

class PharmacistDashboard : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepoImpl())
    }
    
    private val pharmacistViewModel: PharmacistViewModel by viewModels {
        PharmacistViewModelFactory(MedicineRepo(), AppointmentRepo())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authViewModel.getCurrentUser()
        pharmacistViewModel.fetchAllMedicines()
        pharmacistViewModel.fetchAllAppointments()

        setContent {
            MaterialTheme {
                PharmacistDashboardScreen(authViewModel, pharmacistViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistDashboardScreen(authViewModel: AuthViewModel, pharmacistViewModel: PharmacistViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)
    val context = LocalContext.current
    
    val currentUser = authViewModel.currentUser.observeAsState()
    val medicines = pharmacistViewModel.medicines.observeAsState(emptyList())
    val appointments = pharmacistViewModel.appointments.observeAsState(emptyList())
    val operationState = pharmacistViewModel.operationState.observeAsState()

    val upcomingAppointments = appointments.value.filter { it.status == "Upcoming" }
    val lowStockMedicines = medicines.value.filter { it.stock <= it.minStock }

    LaunchedEffect(operationState.value) {
        if (operationState.value is PharmacistViewModel.OperationState.Success) {
            Toast.makeText(context, (operationState.value as PharmacistViewModel.OperationState.Success).message, Toast.LENGTH_SHORT).show()
            pharmacistViewModel.resetOperationState()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PharmacistDrawerContent(
                currentScreen = "Dashboard",
                pharmacistName = currentUser.value?.fullName ?: "Pharmacist",
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
                    title = { Text("Pharmacy Dashboard", color = Color.White, fontSize = 18.sp) },
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
                    .padding(16.dp)
            ) {
                PharmacistWelcomeCard(pharmacistName = currentUser.value?.fullName ?: "Pharmacist")

                Spacer(modifier = Modifier.height(16.dp))

                PharmacistStatsGrid(totalMedicines = medicines.value.size, lowStockCount = lowStockMedicines.size)

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Patient Appointments", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                if (upcomingAppointments.isEmpty()) {
                    Text("No upcoming appointments", color = Color.Gray, modifier = Modifier.padding(8.dp))
                } else {
                    upcomingAppointments.forEach { appt ->
                        PatientBookingCard(
                            appointment = appt,
                            onComplete = { pharmacistViewModel.updateAppointmentStatus(appt.id, "Completed") },
                            onReject = { pharmacistViewModel.updateAppointmentStatus(appt.id, "Cancelled") }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, "Warning", tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Low Stock Medicines", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                lowStockMedicines.forEach { medicine ->
                    MedicineStockCard(
                        medicineName = medicine.name,
                        category = medicine.category,
                        stockLeft = medicine.stock,
                        minRequired = medicine.minStock
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PharmacistWelcomeCard(pharmacistName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Welcome back,", fontSize = 14.sp, color = Color.Gray)
                Text(text = pharmacistName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A5F))
            }
            Icon(Icons.Default.AccountCircle, contentDescription = "Profile", modifier = Modifier.size(40.dp), tint = Color(0xFF1E3A5F))
        }
    }
}

@Composable
fun PharmacistStatsGrid(totalMedicines: Int, lowStockCount: Int) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PharmacistStatCard("Total Medicines", totalMedicines.toString(), Icons.Default.Medication, Modifier.weight(1f))
            PharmacistStatCard("Low Stock Items", lowStockCount.toString(), Icons.Default.Warning, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PharmacistStatCard("Today's Sales", "Rs 0", Icons.Default.Receipt, Modifier.weight(1f))
            PharmacistStatCard("Pending Orders", "0", Icons.Default.ShoppingCart, Modifier.weight(1f))
        }
    }
}

@Composable
fun PharmacistStatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(text = title, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF26D0CE), modifier = Modifier.size(20.dp))
            }
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MedicineStockCard(medicineName: String, category: String, stockLeft: Int, minRequired: Int) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = medicineName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = category, fontSize = 12.sp, color = Color.Gray)
                }
                Text(text = "$stockLeft left", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = if (minRequired > 0) stockLeft.toFloat() / minRequired.toFloat() else 1f
            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Color(0xFF3B82F6),
                trackColor = Color(0xFFE5E7EB)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Minimum required: $minRequired", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PatientBookingCard(appointment: Appointment, onComplete: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFE5F7F7), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color(0xFF26D0CE))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = appointment.patientName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "${appointment.date} | ${appointment.time}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Reason: ${appointment.reason}", fontSize = 13.sp, color = Color.DarkGray)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reject", fontSize = 12.sp)
                }
                Button(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Complete", fontSize = 12.sp)
                }
            }
        }
    }
}

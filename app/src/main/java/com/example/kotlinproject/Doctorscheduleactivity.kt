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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory
import com.example.kotlinproject.ViewModel.StaffViewModel
import kotlinx.coroutines.launch

class DoctorScheduleActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepoImpl())
    }
    private val staffViewModel: StaffViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authViewModel.getCurrentUser()
        staffViewModel.fetchPharmacists()

        setContent {
            MaterialTheme {
                DoctorScheduleScreen(authViewModel, staffViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorScheduleScreen(authViewModel: AuthViewModel, staffViewModel: StaffViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)
    val context = LocalContext.current
    
    val currentUser = authViewModel.currentUser.observeAsState()
    val pharmacists = staffViewModel.professionals.observeAsState(emptyList())

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StaffDrawerContent(
                currentScreen = "DoctorSchedule",
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
                    title = { Text("Pharmacist Schedules") },
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
                Text(text = "Manage Pharmacists", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = "Only registered pharmacists with set schedules can be edited", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                if (pharmacists.value.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No pharmacists found", color = Color.Gray)
                    }
                } else {
                    pharmacists.value.forEach { pharmacist ->
                        PharmacistScheduleCard(
                            name = pharmacist.fullName,
                            specialty = pharmacist.specialty,
                            schedule = pharmacist.schedule,
                            onEdit = {
                                Toast.makeText(context, "Editing ${pharmacist.fullName}", Toast.LENGTH_SHORT).show()
                                // Logic to navigate to an edit screen or open a dialog could go here
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
fun PharmacistScheduleCard(
    name: String,
    specialty: String,
    schedule: Map<String, String>,
    onEdit: () -> Unit
) {
    val isScheduleSet = schedule.isNotEmpty()
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = "Pharmacist",
                        tint = if (isScheduleSet) Color(0xFF26D0CE) else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = specialty, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                
                Button(
                    onClick = onEdit,
                    enabled = isScheduleSet,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E3A5F),
                        disabledContainerColor = Color(0xFFE5E7EB)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(if (isScheduleSet) "Edit" else "Blocked", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isScheduleSet) {
                Text(
                    "Pharmacist has not fixed their time and day yet.",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.forEach { day ->
                        val fullDay = when(day) {
                            "Sun" -> "Sunday"
                            "Mon" -> "Monday"
                            "Tue" -> "Tuesday"
                            "Wed" -> "Wednesday"
                            "Thu" -> "Thursday"
                            "Fri" -> "Friday"
                            else -> ""
                        }
                        val time = schedule[fullDay] ?: "Off"
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = if (time != "Off") Color(0xFFE0F7FA) else Color(0xFFF3F4F6),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = day, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = time,
                                        fontSize = 8.sp,
                                        color = if (time != "Off") Color(0xFF006064) else Color.Gray,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

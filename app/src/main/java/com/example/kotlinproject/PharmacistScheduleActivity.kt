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
import com.example.kotlinproject.Repo.AppointmentRepo
import com.example.kotlinproject.Repo.MedicineRepo
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory
import com.example.kotlinproject.ViewModel.PharmacistViewModel
import com.example.kotlinproject.ViewModel.PharmacistViewModelFactory
import kotlinx.coroutines.launch

class PharmacistScheduleActivity : ComponentActivity() {
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
        
        setContent {
            MaterialTheme {
                PharmacistScheduleScreen(authViewModel, pharmacistViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistScheduleScreen(authViewModel: AuthViewModel, pharmacistViewModel: PharmacistViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkBlue = Color(0xFF1E3A5F)
    val lightGray = Color(0xFFF5F5F5)
    val teal = Color(0xFF26D0CE)

    val currentUser = authViewModel.currentUser.observeAsState()
    val operationState = pharmacistViewModel.operationState.observeAsState()

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedSchedule by remember { mutableStateOf(mutableMapOf<String, String>()) }

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
                currentScreen = "Schedule",
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
                    title = { Text("My Working Hours", color = Color.White, fontSize = 18.sp) },
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
                Text(text = "Manage Schedule", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = "Set your availability for patients to see", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(20.dp))

                days.forEach { day ->
                    var isAvailable by remember { mutableStateOf(false) }
                    var timeRange by remember { mutableStateOf("09:00 AM - 05:00 PM") }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isAvailable,
                                onCheckedChange = { 
                                    isAvailable = it
                                    if (it) selectedSchedule[day] = timeRange else selectedSchedule.remove(day)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = teal)
                            )
                            Text(text = day, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            
                            if (isAvailable) {
                                OutlinedTextField(
                                    value = timeRange,
                                    onValueChange = { 
                                        timeRange = it
                                        selectedSchedule[day] = it
                                    },
                                    modifier = Modifier.width(180.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                    singleLine = true
                                )
                            } else {
                                Text("Unavailable", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        currentUser.value?.uid?.let { uid ->
                            pharmacistViewModel.updatePharmacistSchedule(uid, selectedSchedule)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = teal),
                    shape = RoundedCornerShape(14.dp),
                    enabled = operationState.value !is PharmacistViewModel.OperationState.Loading
                ) {
                    if (operationState.value is PharmacistViewModel.OperationState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save My Schedule", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

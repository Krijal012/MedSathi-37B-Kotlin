package com.example.kotlinproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.kotlinproject.Model.MedicalRecord
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory
import com.example.kotlinproject.ViewModel.StaffViewModel
import kotlinx.coroutines.launch

class PatientRecordsActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepoImpl())
    }
    private val staffViewModel: StaffViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel.getCurrentUser()
        staffViewModel.fetchMedicalRecords()
        
        setContent {
            MaterialTheme {
                PatientRecordsScreen(authViewModel, staffViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordsScreen(authViewModel: AuthViewModel, staffViewModel: StaffViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)
    val context = LocalContext.current
    val currentUser = authViewModel.currentUser.observeAsState()
    val records = staffViewModel.medicalRecords.observeAsState(emptyList())
    
    var searchQuery by remember { mutableStateOf("") }
    val filteredRecords = records.value.filter { 
        it.patientName.contains(searchQuery, ignoreCase = true) || 
        it.diagnosis.contains(searchQuery, ignoreCase = true)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StaffDrawerContent(
                currentScreen = "PatientRecords",
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
                    title = { Text("Patient Records") },
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
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Patient Records", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = "View and update patient medical records", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or diagnosis...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Patient", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            Text("Last Visit", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Diagnosis", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Pharmacist", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(70.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE5E7EB))

                        if (filteredRecords.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No records found", color = Color.Gray)
                            }
                        } else {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                filteredRecords.forEach { record ->
                                    PatientRecordRow(record) {
                                        Toast.makeText(context, "Update logic for ${record.patientName}", Toast.LENGTH_SHORT).show()
                                    }
                                    HorizontalDivider(color = Color(0xFFF3F4F6))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PatientRecordRow(record: MedicalRecord, onUpdate: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text(record.patientName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Text(record.date, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(record.diagnosis, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(record.doctorName, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        
        Button(
            onClick = onUpdate,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(32.dp).width(70.dp)
        ) {
            Text("Update", fontSize = 10.sp, color = Color.White)
        }
    }
}
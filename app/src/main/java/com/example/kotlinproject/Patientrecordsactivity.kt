package com.example.kotlinproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class PatientRecordsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PatientRecordsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordsScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StaffDrawerContent(currentScreen = "PatientRecords") {
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
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Patient Records",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View and update patient medical records",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Search Bar
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search Patients...", fontSize = 14.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = Color(0xFF1E3A5F)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Table Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Patient", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            Text("Last\nVisit", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Diagnosi\ns", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Doctor", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                            Box(modifier = Modifier.width(80.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE5E7EB))

                        // Patient Records
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            repeat(4) {
                                Spacer(modifier = Modifier.height(16.dp))
                                PatientRecordRow(
                                    patientName = "John Smith",
                                    lastVisit = "Jan 10,\n2025",
                                    diagnosis = "Flu\nSympto\nms",
                                    doctor = "Ram\nShresth\na"
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientRecordRow(
    patientName: String,
    lastVisit: String,
    diagnosis: String,
    doctor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(patientName, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.2f))
        Text(lastVisit, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f), lineHeight = 16.sp)
        Text(diagnosis, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f), lineHeight = 16.sp)
        Text(doctor, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(0.8f), lineHeight = 16.sp)

        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.width(80.dp)
        ) {
            Text("Update", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}
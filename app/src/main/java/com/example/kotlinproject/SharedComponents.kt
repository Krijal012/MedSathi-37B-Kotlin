package com.example.kotlinproject

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SharedPatientDrawer(currentScreen: String, onClose: () -> Unit) {
    val darkBlue = Color(0xFF1E3A5F)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(darkBlue)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "MedSathi",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        SharedDrawerMenuItem("Dashboard", Icons.Default.Home, currentScreen == "Dashboard") {
            onClose()
            if (currentScreen != "Dashboard") {
                context.startActivity(Intent(context, PatientDashboard::class.java))
            }
        }

        SharedDrawerMenuItem("Book Appointment", Icons.Default.DateRange, currentScreen == "BookAppointment") {
            onClose()
            if (currentScreen != "BookAppointment") {
                context.startActivity(Intent(context, BookAppointmentActivity::class.java))
            }
        }

        SharedDrawerMenuItem("My Appointments", Icons.Default.List, currentScreen == "MyAppointments") {
            onClose()
            if (currentScreen != "MyAppointments") {
                context.startActivity(Intent(context, MyAppointmentsActivity::class.java))
            }
        }

        SharedDrawerMenuItem("Medical History", Icons.Default.Folder, currentScreen == "MedicalHistory") {
            onClose()
            if (currentScreen != "MedicalHistory") {
                context.startActivity(Intent(context, MedicalHistoryActivity::class.java))
            }
        }

        SharedDrawerMenuItem("Doctor Availability", Icons.Default.Person, currentScreen == "DoctorAvailability") {
            onClose()
            if (currentScreen != "DoctorAvailability") {
                context.startActivity(Intent(context, DoctorAvailabilityActivity::class.java))
            }
        }
    }
}

@Composable
fun SharedDrawerMenuItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF2C5F8D) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTopBar(
    title: String,
    onMenuClick: () -> Unit,
    patientName: String = "{patient's name}"
) {
    val darkBlue = Color(0xFF1E3A5F)

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "Welcome back, $patientName",
                    fontSize = 14.sp,
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = darkBlue,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}
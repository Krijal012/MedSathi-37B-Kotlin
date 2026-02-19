package com.example.kotlinproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

class PatientDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppNavigation() }
    }
}

/* ------------------------------------------------ */
/* NAVIGATION ROOT */
/* ------------------------------------------------ */

@Composable
fun AppNavigation() {

    val nav = rememberNavController()

    Scaffold(bottomBar = { BottomBar(nav) }) { pad ->

        NavHost(navController = nav,
            startDestination = "dashboard",
            modifier = Modifier.padding(pad)) {

            composable("dashboard") { DashboardScreen(nav) }
            composable("profile") { ProfileScreen() }
            composable("appointments") { AppointmentScreen() }
            composable("doctors") { DoctorsScreen() }
            composable("records") { RecordsScreen() }
        }
    }
}

/* ------------------------------------------------ */
/* DASHBOARD */
/* ------------------------------------------------ */

@Composable
fun DashboardScreen(nav: NavHostController) {

    val gradient = Brush.horizontalGradient(
        listOf(Color(0xFF6A5AE0), Color(0xFF8E7CFF))
    )

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF6F7FB))
    ) {

        /* HEADER */

        Box(
            Modifier.fillMaxWidth().height(210.dp).background(gradient).padding(20.dp)
        ) {

            Column {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        Modifier.size(60.dp).background(Color.White.copy(.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person,null,tint=Color.White,modifier=Modifier.size(32.dp))
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text("Welcome Back", color = Color.White.copy(.9f))
                        Text("John Doe", color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatCard("Heart", "72 bpm")
                    StatCard("BP", "120/80")
                    StatCard("Sugar", "98")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp,
            modifier = Modifier.padding(start = 20.dp, bottom = 12.dp))

        Column(Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                ActionCard("Profile", Icons.Default.Person, Modifier.weight(1f)) {
                    nav.navigate("profile")
                }

                ActionCard("Appointments", Icons.Default.Event, Modifier.weight(1f)) {
                    nav.navigate("appointments")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                ActionCard("Doctors", Icons.Default.LocalHospital, Modifier.weight(1f)) {
                    nav.navigate("doctors")
                }

                ActionCard("Records", Icons.Default.Folder, Modifier.weight(1f)) {
                    nav.navigate("records")
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* PROFILE */
/* ------------------------------------------------ */

@Composable
fun ProfileScreen() {

    Column {

        Header("My Profile")

        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            Box(Modifier.size(110.dp).background(Color(0xFFEDEBFF), CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person,null,Modifier.size(60.dp),tint=Color(0xFF6A5AE0))
            }

            Spacer(Modifier.height(16.dp))

            Text("John Doe", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Patient ID: P1023", color = Color.Gray)

            Spacer(Modifier.height(24.dp))

            InfoCard {
                InfoRow("Age","24")
                InfoRow("Gender","Male")
                InfoRow("Blood Group","O+")
                InfoRow("Phone","+977 9800000000")
                InfoRow("Email","john@email.com")
            }
        }
    }
}

/* ------------------------------------------------ */
/* APPOINTMENTS */
/* ------------------------------------------------ */

@Composable
fun AppointmentScreen() {

    Column {

        Header("Appointments")

        LazyColumn(Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            items(listOf("Cardiologist","Dentist","Neurologist")) {

                InfoCard {
                    Text("Dr. Smith", fontWeight = FontWeight.Bold)
                    Text(it, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("21 Feb • 10:30 AM", color = Color(0xFF6A5AE0))
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* DOCTORS */
/* ------------------------------------------------ */

@Composable
fun DoctorsScreen() {

    Column {

        Header("Doctors")

        LazyColumn(Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            items((1..6).toList()) {

                InfoCard {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Box(Modifier.size(55.dp).background(Color(0xFFEDEBFF),CircleShape),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person,null,tint=Color(0xFF6A5AE0))
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text("Dr. John Doe", fontWeight = FontWeight.Bold)
                            Text("Specialist", color = Color.Gray)
                        }

                        Button(onClick = {}) { Text("View") }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* RECORDS */
/* ------------------------------------------------ */

@Composable
fun RecordsScreen() {

    Column {

        Header("Medical Records")

        LazyColumn(Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            items(listOf("Blood Test","X-Ray","MRI","Prescription")) {

                InfoCard {
                    Text(it, fontWeight = FontWeight.Bold)
                    Text("City Hospital", color = Color.Gray)
                    Text("Feb 15 2026", color = Color.Gray)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = {}) { Text("View") }
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* REUSABLE UI */
/* ------------------------------------------------ */

@Composable
fun Header(title:String){
    Box(
        Modifier.fillMaxWidth().height(120.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFF6A5AE0),Color(0xFF8E7CFF)))),
        contentAlignment = Alignment.CenterStart
    ){
        Text(title,color=Color.White,fontSize=26.sp,fontWeight=FontWeight.Bold,
            modifier=Modifier.padding(start=20.dp))
    }
}

@Composable
fun InfoCard(content:@Composable ColumnScope.()->Unit){
    Card(shape=RoundedCornerShape(20.dp),
        elevation=CardDefaults.cardElevation(6.dp),
        modifier=Modifier.fillMaxWidth()){
        Column(Modifier.padding(18.dp),content=content)
    }
}

@Composable
fun InfoRow(label:String,value:String){
    Row(Modifier.fillMaxWidth().padding(vertical=10.dp),
        horizontalArrangement=Arrangement.SpaceBetween){
        Text(label,fontWeight=FontWeight.SemiBold)
        Text(value,color=Color.Gray)
    }
}

@Composable
fun StatCard(label:String,value:String){
    Card(shape=RoundedCornerShape(16.dp),
        colors=CardDefaults.cardColors(Color.White.copy(.15f))){
        Column(Modifier.padding(horizontal=18.dp,vertical=10.dp),
            horizontalAlignment=Alignment.CenterHorizontally){
            Text(value,color=Color.White,fontWeight=FontWeight.Bold)
            Text(label,color=Color.White.copy(.9f),fontSize=12.sp)
        }
    }
}

@Composable
fun ActionCard(title:String,icon:ImageVector,modifier:Modifier,click:()->Unit){
    Card(modifier.height(130.dp).clickable{click()},
        shape=RoundedCornerShape(20.dp),
        elevation=CardDefaults.cardElevation(6.dp)){
        Column(Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement=Arrangement.Center){
            Icon(icon,null,tint=Color(0xFF6A5AE0),modifier=Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(title,fontWeight=FontWeight.Bold)
        }
    }
}

/* ------------------------------------------------ */
/* BOTTOM NAV */
/* ------------------------------------------------ */

@Composable
fun BottomBar(nav:NavHostController){
    NavigationBar{

        NavigationBarItem(selected=false,
            onClick={nav.navigate("dashboard")},
            icon={Icon(Icons.Default.Home,null)},
            label={Text("Home")})

        NavigationBarItem(selected=false,
            onClick={nav.navigate("appointments")},
            icon={Icon(Icons.Default.Event,null)},
            label={Text("Appointments")})

        NavigationBarItem(selected=false,
            onClick={nav.navigate("doctors")},
            icon={Icon(Icons.Default.LocalHospital,null)},
            label={Text("Doctors")})

        NavigationBarItem(selected=false,
            onClick={nav.navigate("records")},
            icon={Icon(Icons.Default.Folder,null)},
            label={Text("Records")})
    }
}

package com.example.kotlinproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class PharmacistDashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PharmacistDashboardScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistDashboardScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PharmacistDrawerContent(currentScreen = "Dashboard") {
                scope.launch { drawerState.close() }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "MedSathi - Pharmacy",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        Text(
                            text = "Welcome, Pharmacist",
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                PharmacistWelcomeCard(pharmacistName = "Pharmacist")

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Pharmacy Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = "Manage medicines, inventory, and billing", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                PharmacistStatsGrid()

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Low Stock Items", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                repeat(4) {
                    MedicineStockCard(
                        medicineName = "Amoxicillin 500mg",
                        category = "Antibiotics",
                        stockLeft = 15,
                        minRequired = 50
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PharmacistDrawerContent(currentScreen: String = "Dashboard", onClose: () -> Unit = {}) {
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
            text = "MedSathi - Pharmacy",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        PharmacistDrawerMenuItem("Dashboard", Icons.Default.Home, currentScreen == "Dashboard") {
            onClose()
            if (currentScreen != "Dashboard") context.startActivity(Intent(context, PharmacistDashboard::class.java))
        }
        PharmacistDrawerMenuItem("Add Medicine", Icons.Default.Add, currentScreen == "AddMedicine") {
            onClose()
            if (currentScreen != "AddMedicine") context.startActivity(Intent(context, AddMedicineActivity::class.java))
        }
        PharmacistDrawerMenuItem("Search Medicine", Icons.Default.Search, currentScreen == "SearchMedicine") {
            onClose()
            if (currentScreen != "SearchMedicine") context.startActivity(Intent(context, SearchMedicineActivity::class.java))
        }
        PharmacistDrawerMenuItem("Billing", Icons.Default.Receipt, currentScreen == "Billing") {
            onClose()
            if (currentScreen != "Billing") context.startActivity(Intent(context, BillingActivity::class.java))
        }
    }
}

@Composable
fun PharmacistDrawerMenuItem(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF2C5F8D) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, color = Color.White, fontSize = 14.sp)
    }
    Spacer(modifier = Modifier.height(8.dp))
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
fun PharmacistStatsGrid() {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PharmacistStatCard("Total Medicines", "2025", Icons.Default.Medication, Modifier.weight(1f))
            PharmacistStatCard("Low Stock Items", "23", Icons.Default.Warning, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PharmacistStatCard("Today's Sales", "Rs 50000", Icons.Default.Receipt, Modifier.weight(1f))
            PharmacistStatCard("Pending Orders", "15", Icons.Default.ShoppingCart, Modifier.weight(1f))
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

            LinearProgressIndicator(
                progress = { stockLeft.toFloat() / minRequired.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Color(0xFF3B82F6),
                trackColor = Color(0xFFE5E7EB)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Minimum required: $minRequired", fontSize = 11.sp, color = Color.Gray)
        }
    }
}
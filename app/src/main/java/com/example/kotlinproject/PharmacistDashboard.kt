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
import androidx.compose.ui.graphics.vector.ImageVector
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
            PharmacistDrawerContent()
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("MedSathi - Pharmacy") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
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
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(lightGray)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Welcome Header
                PharmacistWelcomeCard(doctorName = "doctor's name")

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Doctor Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage medicines, inventory, and billing",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Grid
                PharmacistStatsGrid()

                Spacer(modifier = Modifier.height(16.dp))

                // Low Stock Alert
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Low Stock Items",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Medicine Stock Items
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
fun PharmacistDrawerContent() {
    val darkBlue = Color(0xFF1E3A5F)

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

        PharmacistDrawerMenuItem("Dashboard", Icons.Default.Home, true)
        PharmacistDrawerMenuItem("Add Medicine", Icons.Default.Add, false)
        PharmacistDrawerMenuItem("Search Medicine", Icons.Default.Search, false)
        PharmacistDrawerMenuItem("Billing", Icons.Default.Receipt, false)
    }
}

@Composable
fun PharmacistDrawerMenuItem(title: String, icon: ImageVector, isSelected: Boolean) {
    val backgroundColor = if (isSelected) Color(0xFF2C5F8D) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
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

@Composable
fun PharmacistWelcomeCard(doctorName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back, {$doctorName}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF1E3A5F)
            )
        }
    }
}

@Composable
fun PharmacistStatsGrid() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PharmacistStatCard(
                "Total Medicines",
                "2025",
                Icons.Default.Medication,
                Modifier.weight(1f)
            )
            PharmacistStatCard(
                "Low Stock Items",
                "23",
                Icons.Default.Warning,
                Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PharmacistStatCard(
                "Today's Sales",
                "Rs 50000",
                Icons.Default.Receipt,
                Modifier.weight(1f)
            )
            PharmacistStatCard(
                "Pending Order",
                "15",
                Icons.Default.ShoppingCart,
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PharmacistStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF26D0CE),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MedicineStockCard(
    medicineName: String,
    category: String,
    stockLeft: Int,
    minRequired: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicineName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "$stockLeft left",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            Column {
                LinearProgressIndicator(
                    progress = { stockLeft.toFloat() / minRequired.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Color(0xFF3B82F6),
                    trackColor = Color(0xFFE5E7EB),
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Minimum required: $minRequired",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
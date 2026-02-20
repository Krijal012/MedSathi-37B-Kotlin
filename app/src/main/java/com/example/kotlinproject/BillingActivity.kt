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

class BillingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BillingScreen()
            }
        }
    }
}

data class BillItem(val name: String, val qty: Int, val price: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)
    val teal = Color(0xFF26D0CE)

    var patientName by remember { mutableStateOf("") }
    var medicineSearch by remember { mutableStateOf("") }
    var billItems by remember { mutableStateOf(listOf(
        BillItem("Amoxicillin 500mg", 2, 150.0),
        BillItem("Paracetamol 500mg", 1, 50.0)
    )) }

    val subtotal = billItems.sumOf { it.qty * it.price }
    val tax = subtotal * 0.13
    val total = subtotal + tax

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PharmacistDrawerContent(currentScreen = "Billing") {
                scope.launch { drawerState.close() }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("MedSathi - Pharmacy", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        Text("Welcome, Pharmacist", fontSize = 12.sp, color = Color.White, modifier = Modifier.padding(end = 8.dp))
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp).padding(end = 8.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBlue, titleContentColor = Color.White)
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
                Text(text = "Billing", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = "Create and manage patient bills", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(20.dp))

                // Patient Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Patient Information", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        HorizontalDivider(color = Color(0xFFE5E7EB))

                        OutlinedTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = { Text("Patient Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = teal) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = medicineSearch,
                            onValueChange = { medicineSearch = it },
                            label = { Text("Search & Add Medicine") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = teal) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (medicineSearch.isNotEmpty()) {
                                        billItems = billItems + BillItem(medicineSearch, 1, 100.0)
                                        medicineSearch = ""
                                    }
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = teal)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bill Items Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Bill Items", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${billItems.size} items", fontSize = 13.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFE5E7EB))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Table Header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Medicine", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                            Text("Qty", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                            Text("Price", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                            Text("Total", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                            Box(modifier = Modifier.width(32.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        billItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                Text("${item.qty}", fontSize = 13.sp, modifier = Modifier.weight(0.5f), color = Color.Gray)
                                Text("Rs${item.price.toInt()}", fontSize = 13.sp, modifier = Modifier.weight(0.7f), color = Color.Gray)
                                Text("Rs${(item.qty * item.price).toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.7f))
                                IconButton(
                                    onClick = { billItems = billItems.toMutableList().also { it.removeAt(index) } },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                            if (index < billItems.size - 1) HorizontalDivider(color = Color(0xFFE5E7EB))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Bill Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        HorizontalDivider(color = Color(0xFFE5E7EB))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 14.sp, color = Color.Gray)
                            Text("Rs ${subtotal.toInt()}", fontSize = 14.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax (13% VAT)", fontSize = 14.sp, color = Color.Gray)
                            Text("Rs ${tax.toInt()}", fontSize = 14.sp)
                        }
                        HorizontalDivider(color = Color(0xFFE5E7EB))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Rs ${total.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = teal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { billItems = emptyList(); patientName = "" },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Clear Bill", fontSize = 16.sp)
                    }
                    Button(
                        onClick = { /* TODO: Print / Save bill */ },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = teal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print Bill", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kotlinproject.Model.Medicine
import com.example.kotlinproject.Repo.AppointmentRepo
import com.example.kotlinproject.Repo.MedicineRepo
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory
import com.example.kotlinproject.ViewModel.PharmacistViewModel
import com.example.kotlinproject.ViewModel.PharmacistViewModelFactory
import kotlinx.coroutines.launch

class SearchMedicineActivity : ComponentActivity() {
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
        
        setContent {
            MaterialTheme {
                SearchMedicineScreen(authViewModel, pharmacistViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMedicineScreen(authViewModel: AuthViewModel, pharmacistViewModel: PharmacistViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val lightGray = Color(0xFFF5F5F5)
    val darkBlue = Color(0xFF1E3A5F)
    val teal = Color(0xFF26D0CE)
    val context = LocalContext.current

    val currentUser = authViewModel.currentUser.observeAsState()
    val medicines = pharmacistViewModel.medicines.observeAsState(emptyList())
    val operationState = pharmacistViewModel.operationState.observeAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Antibiotics", "Painkillers", "Vitamins", "Antacids", "Antivirals")

    var showEditDialog by remember { mutableStateOf(false) }
    var medicineToEdit by remember { mutableStateOf<Medicine?>(null) }

    LaunchedEffect(operationState.value) {
        if (operationState.value is PharmacistViewModel.OperationState.Success) {
            Toast.makeText(context, (operationState.value as PharmacistViewModel.OperationState.Success).message, Toast.LENGTH_SHORT).show()
            pharmacistViewModel.resetOperationState()
        }
    }

    val filteredMedicines = medicines.value.filter {
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == "All" || it.category == selectedFilter
        matchesSearch && matchesFilter
    }

    if (showEditDialog && medicineToEdit != null) {
        EditMedicineDialog(
            medicine = medicineToEdit!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { updated ->
                pharmacistViewModel.updateMedicine(updated)
                showEditDialog = false
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PharmacistDrawerContent(
                currentScreen = "SearchMedicine",
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
                    title = { Text("Inventory Search", color = Color.White, fontSize = 18.sp) },
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
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = teal) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("${filteredMedicines.size} medicines found", fontSize = 13.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredMedicines.forEach { med ->
                        MedicineSearchCard(
                            medicine = med,
                            onEdit = { 
                                medicineToEdit = med
                                showEditDialog = true
                            },
                            onDelete = { pharmacistViewModel.deleteMedicine(med.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineSearchCard(medicine: Medicine, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (medicine.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = medicine.imageUrl,
                    contentDescription = medicine.name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Medication, null, tint = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(medicine.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(medicine.category, fontSize = 12.sp, color = Color.Gray)
                Text("Stock: ${medicine.stock} | Rs ${medicine.price}", fontSize = 12.sp, color = Color(0xFF26D0CE))
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Blue) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }
        }
    }
}

@Composable
fun EditMedicineDialog(medicine: Medicine, onDismiss: () -> Unit, onConfirm: (Medicine) -> Unit) {
    var stock by remember { mutableStateOf(medicine.stock.toString()) }
    var price by remember { mutableStateOf(medicine.price.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${medicine.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(medicine.copy(stock = stock.toIntOrNull() ?: medicine.stock, price = price.toDoubleOrNull() ?: medicine.price))
            }) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

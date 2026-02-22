package com.example.kotlinproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.ViewModel.AuthViewModel
import com.example.kotlinproject.ViewModel.AuthViewModelFactory

class LoginActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepoImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginScreen(viewModel)
        }
    }
}

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val authState = viewModel.authState.observeAsState()

    // Handle auth state changes
    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthViewModel.AuthState.Success -> {
                val user = state.user

                // Navigate based on role
                val intent = when (user?.role) {
                    "patient" -> Intent(context, PatientDashboard::class.java)
                    "staff" -> Intent(context, StaffDashboard::class.java)
                    "admin" -> Intent(context, AdminDashboard::class.java)
                    "pharmacist" -> Intent(context, PharmacistDashboard::class.java)
                    else -> Intent(context, PatientDashboard::class.java)
                }

                context.startActivity(intent)
                (context as? ComponentActivity)?.finish()
            }
            is AuthViewModel.AuthState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FC),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Image(painterResource(R.drawable.logo), null, modifier = Modifier.size(110.dp))

            Spacer(modifier = Modifier.height(20.dp))

            Text("Welcome Back", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Login to continue", fontSize = 14.sp, color = Color(0xFF6E6E73))

            Spacer(modifier = Modifier.height(40.dp))

            // Email
            modernTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                iconRes = R.drawable.baseline_email_24
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                leadingIcon = { Icon(painterResource(R.drawable.baseline_lock_24), null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painterResource(
                                if (passwordVisible) R.drawable.baseline_visibility_24
                                else R.drawable.baseline_visibility_off_24
                            ), null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = modernFieldColors()
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Login Button
            Button(
                onClick = {
                    viewModel.login(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6CF7)),
                enabled = authState.value !is AuthViewModel.AuthState.Loading
            ) {
                if (authState.value is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row {
                Text("Don't have an account? ", color = Color(0xFF6E6E73))
                Text(
                    "Register",
                    color = Color(0xFF6A4FE9),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, RegisterActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    }
                )
            }
        }
    }
}

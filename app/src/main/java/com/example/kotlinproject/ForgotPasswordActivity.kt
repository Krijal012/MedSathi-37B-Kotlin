package com.example.kotlinproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ForgotPasswordScreen() }
    }
}

@Composable
fun ForgotPasswordScreen() {
    var email by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Message", fontWeight = FontWeight.Bold) },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(onClick = { showDialog = false }) { Text("OK") }
            }
        )
    }

    Scaffold(containerColor = Color(0xFFF8F9FC)) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Image(painterResource(R.drawable.logo), null, modifier = Modifier.size(110.dp))

            Spacer(modifier = Modifier.height(24.dp))

            Text("Reset Password", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Enter your email to receive reset link", fontSize = 14.sp, color = Color(0xFF6E6E73))

            Spacer(modifier = Modifier.height(50.dp))

            modernTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                iconRes = R.drawable.baseline_email_24
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty()) {
                        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                            dialogMessage = if (task.isSuccessful)
                                "Reset link sent to your email"
                            else
                                "Error: ${task.exception?.message}"
                            showDialog = true
                        }
                    } else {
                        dialogMessage = "Email is required"
                        showDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6CF7))
            ) {
                Text("Send Reset Link", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

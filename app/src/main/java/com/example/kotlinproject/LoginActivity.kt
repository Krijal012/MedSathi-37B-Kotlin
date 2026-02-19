package com.example.kotlinproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LoginScreen() }
    }
}

@Composable
fun LoginScreen() {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Message", fontWeight = FontWeight.Bold) },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    if (dialogMessage.contains("Successful")) {
                        context.startActivity(Intent(context, MainActivity::class.java))
                        activity.finish()
                    }
                }) { Text("OK") }
            }
        )
    }

    Scaffold(containerColor = Color(0xFFF8F9FC)) { padding ->

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            Image(painterResource(R.drawable.logo), null, modifier = Modifier.size(110.dp))

            Spacer(modifier = Modifier.height(20.dp))

            Text("Welcome Back", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Login to continue", fontSize = 14.sp, color = Color(0xFF6E6E73))

            Spacer(modifier = Modifier.height(40.dp))

            modernTextField(email, { email = it }, "Email Address", R.drawable.baseline_email_24)

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    "Forgot Password?",
                    color = Color(0xFF6A4FE9),
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, ForgotPasswordActivity::class.java))
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(activity) { task ->
                                dialogMessage = if (task.isSuccessful)
                                    "Login Successful"
                                else
                                    "Login Failed: ${task.exception?.message}"
                                showDialog = true
                            }
                    } else {
                        dialogMessage = "Please fill all fields"
                        showDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6CF7))
            ) {
                Text("Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
                        activity.finish()
                    }
                )
            }
        }
    }
}

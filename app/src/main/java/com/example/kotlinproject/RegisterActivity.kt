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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinproject.Repo.UserRepo
import com.example.kotlinproject.Repo.UserRepoImpl
import com.example.kotlinproject.modernTextField
import com.example.kotlinproject.modernFieldColors


class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RegisterScreen() }
    }
}

@Composable
fun RegisterScreen() {

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as Activity
    val userRepo: UserRepo = UserRepoImpl()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Message", fontWeight = FontWeight.Bold) },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    if (dialogMessage.contains("Successful")) {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        activity.finish()
                    }
                }) { Text("OK") }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FC)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Create Account",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Text(
                text = "Sign up to get started",
                fontSize = 14.sp,
                color = Color(0xFF6E6E73)
            )

            Spacer(modifier = Modifier.height(40.dp))

            modernTextField(fullName, { fullName = it }, "Full Name", R.drawable.baseline_person_24)
            Spacer(modifier = Modifier.height(16.dp))
            modernTextField(email, { email = it }, "Email Address", R.drawable.baseline_email_24)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                leadingIcon = {
                    Icon(painterResource(R.drawable.baseline_lock_24), null)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painterResource(
                                if (passwordVisible) R.drawable.baseline_visibility_24
                                else R.drawable.baseline_visibility_off_24
                            ),
                            null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = modernFieldColors()
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                        userRepo.register(fullName, email, password) { success, message, _ ->
                            dialogMessage = message
                            showDialog = true
                        }
                    } else {
                        dialogMessage = "All fields are required"
                        showDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6CF7))
            ) {
                Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row {
                Text("Already have an account? ", color = Color(0xFF6E6E73))
                Text(
                    "Login",
                    color = Color(0xFF6A4FE9),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        activity.finish()
                    }
                )
            }
        }
    }
}

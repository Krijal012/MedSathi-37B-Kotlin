package com.example.kotlinproject

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun modernTextField(
    value: String,
    onChange: (String) -> Unit,
    hint: String,
    icon: Int
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(hint) },
        leadingIcon = {
            Icon(painterResource(icon), contentDescription = null)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = modernFieldColors()
    )
}

@Composable
fun modernFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedIndicatorColor = Color(0xFF4A6CF7),
    unfocusedIndicatorColor = Color(0xFFE0E0E0),
    cursorColor = Color(0xFF4A6CF7)
)

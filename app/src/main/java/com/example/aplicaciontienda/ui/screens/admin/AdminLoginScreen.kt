package com.example.aplicaciontienda.ui.screens.admin

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/** Equivalente a AdminLogin.jsx / AdminLoginActivity: credenciales fijas (administrador/2026, igual que la web). */
@Composable
fun AdminLoginScreen(onLoginSuccess: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("remembered", false)) {
            onLoginSuccess()
        }
    }

    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Acceso Administrador", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = user,
            onValueChange = { user = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
            Text("Recordarme")
        }
        if (error) {
            Spacer(Modifier.height(8.dp))
            Text("Credenciales incorrectas", color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (user == "administrador" && pass == "2026") {
                    error = false
                    if (rememberMe) {
                        context.getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE)
                            .edit().putBoolean("remembered", true).apply()
                    }
                    onLoginSuccess()
                } else {
                    error = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Ingresar") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) { Text("Volver") }
    }
}

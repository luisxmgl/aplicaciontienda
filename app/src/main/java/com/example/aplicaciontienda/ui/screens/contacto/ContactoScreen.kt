package com.example.aplicaciontienda.ui.screens.contacto

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/** Equivalente a Contacto.jsx: formulario de contacto, enviado vía server/contact.js (mismo servidor que Webpay). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactoScreen(onBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacto") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(20.dp)) {
            Text("¿Tienes dudas o comentarios? Escríbenos y te responderemos a la brevedad.")
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = mensaje, onValueChange = { mensaje = it }, label = { Text("Mensaje") },
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )
            Spacer(Modifier.height(16.dp))
            resultado?.let {
                Text(it, color = if (it.startsWith("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            Button(
                enabled = !enviando && nombre.isNotBlank() && email.isNotBlank() && mensaje.isNotBlank(),
                onClick = {
                    enviando = true
                    resultado = null
                    scope.launch {
                        val ok = withContext(Dispatchers.IO) {
                            try {
                                val body = JSONObject().apply {
                                    put("nombre", nombre); put("email", email); put("mensaje", mensaje)
                                }.toString().toRequestBody("application/json".toMediaType())
                                val request = Request.Builder()
                                    .url("${BuildConfig.WEBPAY_BASE_URL}/api/contacto")
                                    .post(body).build()
                                OkHttpClient().newCall(request).execute().use { it.isSuccessful }
                            } catch (e: Exception) {
                                false
                            }
                        }
                        enviando = false
                        resultado = if (ok) {
                            nombre = ""; email = ""; mensaje = ""
                            "✅ Mensaje enviado, gracias por escribirnos."
                        } else {
                            "No se pudo enviar el mensaje. Intenta de nuevo o escríbenos por WhatsApp."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text(if (enviando) "Enviando…" else "Enviar mensaje") }
        }
    }
}

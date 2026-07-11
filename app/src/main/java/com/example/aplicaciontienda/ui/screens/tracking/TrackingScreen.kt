package com.example.aplicaciontienda.ui.screens.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.Pedido
import com.example.aplicaciontienda.R
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import com.example.aplicaciontienda.ui.theme.SuccessGreen
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/** Equivalente a Tracking.jsx / TrackingActivity: consulta de estado de pedido (4 pasos). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(initialCode: String?, onBack: () -> Unit) {
    var code by remember { mutableStateOf(initialCode ?: "") }
    var pedido by remember { mutableStateOf<Pedido?>(null) }
    var searching by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun buscar(c: String) {
        if (c.isBlank()) return
        searching = true
        errorMsg = null
        pedido = null
        FirebaseDatabase.getInstance().getReference("pedidos").child(c)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    searching = false
                    if (!snapshot.exists()) {
                        errorMsg = "No se encontró el pedido #$c"
                        return
                    }
                    pedido = snapshot.getValue(Pedido::class.java)
                }
                override fun onCancelled(error: DatabaseError) {
                    searching = false
                    errorMsg = "Error de conexión con el servidor"
                }
            })
    }

    LaunchedEffect(Unit) {
        if (!initialCode.isNullOrBlank()) buscar(initialCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tracking_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.tracking_placeholder)) },
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { buscar(code) }) { Icon(Icons.Default.Search, contentDescription = null) }
            }

            if (searching) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.tracking_searching))
            }
            errorMsg?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            pedido?.let { p ->
                Spacer(Modifier.height(20.dp))
                Card(colors = CardDefaults.cardColors(containerColor = BackgroundPaperRaised)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.tracking_order_label, code), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        TrackingSteps(estado = p.estado)
                        Spacer(Modifier.height(12.dp))
                        Text(estadoNota(p.estado))
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackingSteps(estado: Int) {
    val labels = listOf("Recibido", "Confección/Bordado", "Listo para Retiro", "Entregado")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEachIndexed { index, label ->
            val activo = estado >= index + 1
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    Modifier
                        .size(14.dp)
                        .background(
                            color = if (activo) SuccessGreen else MaterialTheme.colorScheme.outline,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

private fun estadoNota(estado: Int): String = when (estado) {
    1 -> "Hemos recibido tu pago. Tu pedido está en espera de entrar al taller."
    2 -> "¡Buenas noticias! Tu pedido está siendo confeccionado o bordado en este momento."
    3 -> "¡Tu pedido está LISTO! Puedes pasar a la tienda a retirarlo con la cajera."
    4 -> "Tu pedido ha sido entregado. ¡Gracias por confiar en Confecciones Villa Acero!"
    else -> "Consulta en tienda por el estado de tu pedido."
}

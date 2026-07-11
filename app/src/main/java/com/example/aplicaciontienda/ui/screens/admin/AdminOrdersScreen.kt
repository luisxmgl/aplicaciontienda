package com.example.aplicaciontienda.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.Pedido
import com.example.aplicaciontienda.Utils
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import com.example.aplicaciontienda.ui.theme.SuccessGreen
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ESTADOS = listOf(1 to "Recibido", 2 to "Confección/Bordado", 3 to "Listo para Retiro", 4 to "Entregado")

/** Equivalente a AdminOrders.jsx / AdminOrdersActivity: gestión de pedidos con cambio de estado. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(onBack: () -> Unit, onOpenCaja: () -> Unit) {
    val orders = remember { mutableStateListOf<Pedido>() }

    DisposableEffect(Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("pedidos")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Un pedido con Webpay que nunca se pagó (el cliente se arrepintió o no completó
                // el flujo) no es un pedido real: no debe aparecer para que el staff lo prepare.
                val list = snapshot.children.mapNotNull { it.getValue(Pedido::class.java) }
                    .filter { it.metodoPago != "webpay" || it.pagado == true }
                    .sortedByDescending { it.fecha }
                orders.clear()
                orders.addAll(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
                actions = { TextButton(onClick = onOpenCaja) { Text("Abrir caja") } }
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Aún no hay pedidos.")
            }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(orders, key = { it.codigoRetiro }) { pedido -> AdminOrderCard(pedido) }
            }
        }
    }
}

@Composable
private fun AdminOrderCard(pedido: Pedido) {
    Card(colors = CardDefaults.cardColors(containerColor = BackgroundPaperRaised)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pedido #${pedido.codigoRetiro}", fontWeight = FontWeight.Bold)
                Text(SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(pedido.fecha)), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(6.dp))
            pedido.items.forEach { item ->
                Text("${item.cantidad}x ${item.nombre} (${item.talla})", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(4.dp))
            Text("Total: ${Utils.formatPrice(pedido.total + pedido.extraCharge)}", color = AccentThread, fontWeight = FontWeight.Bold)
            Text(
                "Personalización: ${pedido.customization.ifEmpty { "Ninguna" }}",
                style = MaterialTheme.typography.bodySmall
            )
            if (pedido.origen == "pos") {
                Spacer(Modifier.height(4.dp))
                Text("Venta en tienda (Caja) — ${pedido.metodoPago ?: ""}", color = SuccessGreen, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ESTADOS.forEach { (value, label) ->
                    FilterChip(
                        selected = pedido.estado == value,
                        onClick = {
                            if (pedido.estado != value) {
                                FirebaseDatabase.getInstance().getReference("pedidos")
                                    .child(pedido.codigoRetiro).child("estado").setValue(value)
                            }
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentThread.copy(alpha = 0.25f))
                    )
                }
            }
        }
    }
}

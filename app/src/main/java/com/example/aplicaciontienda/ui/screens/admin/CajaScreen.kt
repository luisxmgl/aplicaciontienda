package com.example.aplicaciontienda.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.CartItem
import com.example.aplicaciontienda.CartItemPedido
import com.example.aplicaciontienda.Pedido
import com.example.aplicaciontienda.Producto
import com.example.aplicaciontienda.Utils
import com.example.aplicaciontienda.data.catalog.CatalogUiState
import com.example.aplicaciontienda.data.catalog.CatalogViewModel
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import com.example.aplicaciontienda.ui.theme.SuccessGreen
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

private enum class CajaStep { TICKET, PAGO, RECIBO }
private val METODOS = listOf("efectivo", "tarjeta", "transferencia", "webpay")

/** Equivalente a Caja.jsx: punto de venta en tienda física (efectivo/tarjeta/transferencia) con recibo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CajaScreen(catalogViewModel: CatalogViewModel, onBack: () -> Unit) {
    val state by catalogViewModel.state.collectAsState()
    var step by remember { mutableStateOf(CajaStep.TICKET) }
    val ticket = remember { mutableStateListOf<CartItem>() }
    var query by remember { mutableStateOf("") }
    var metodoPago by remember { mutableStateOf<String?>(null) }
    var montoRecibido by remember { mutableStateOf("") }
    var pedidoFinal by remember { mutableStateOf<Pedido?>(null) }
    var warning by remember { mutableStateOf<String?>(null) }

    val total = ticket.sumOf { it.producto.precio * it.cantidad }

    fun addToTicket(producto: Producto) {
        val existing = ticket.find { it.producto.idproducto == producto.idproducto }
        if (existing != null) existing.cantidad++ else ticket.add(CartItem(producto, 1))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caja") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (step) {
                CajaStep.TICKET -> TicketStep(
                    state = state,
                    query = query,
                    onQueryChange = { query = it },
                    ticket = ticket,
                    total = total,
                    onAdd = ::addToTicket,
                    onIncrement = { it.cantidad++ },
                    onDecrement = { item -> if (item.cantidad > 1) item.cantidad-- else ticket.remove(item) },
                    onRemove = { ticket.remove(it) },
                    onClear = { ticket.clear() },
                    onContinue = { step = CajaStep.PAGO }
                )
                CajaStep.PAGO -> PagoStep(
                    total = total,
                    metodoPago = metodoPago,
                    onMetodoChange = { metodoPago = it },
                    montoRecibido = montoRecibido,
                    onMontoChange = { montoRecibido = it },
                    onBack = { step = CajaStep.TICKET },
                    onConfirm = {
                        val code = Utils.generateOrderCode()
                        val montoRecibidoNum = montoRecibido.toIntOrNull()
                        val vuelto = if (metodoPago == "efectivo" && montoRecibidoNum != null) montoRecibidoNum - total else null
                        val pedido = Pedido(
                            id = code,
                            codigoRetiro = code,
                            items = ticket.map { CartItemPedido(it.producto.nombre, it.producto.talla, it.producto.precio, it.cantidad, it.producto.colegio) },
                            total = total,
                            extraCharge = 0,
                            customization = "",
                            estado = 4,
                            fecha = System.currentTimeMillis(),
                            procesado = true,
                            origen = "pos",
                            metodoPago = metodoPago,
                            pagado = true,
                            montoRecibido = montoRecibidoNum,
                            vuelto = vuelto
                        )
                        try {
                            FirebaseDatabase.getInstance().getReference("pedidos").child(code).setValue(pedido)
                        } catch (e: Exception) {
                            warning = "No se pudo sincronizar con el servidor, pero la venta quedó registrada localmente."
                        }
                        pedidoFinal = pedido
                        step = CajaStep.RECIBO
                    }
                )
                CajaStep.RECIBO -> pedidoFinal?.let { pedido ->
                    ReciboStep(pedido = pedido, warning = warning, onNuevaVenta = {
                        ticket.clear(); metodoPago = null; montoRecibido = ""; pedidoFinal = null; warning = null; step = CajaStep.TICKET
                    })
                }
            }
        }
    }
}

@Composable
private fun TicketStep(
    state: CatalogUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    ticket: List<CartItem>,
    total: Int,
    onAdd: (Producto) -> Unit,
    onIncrement: (CartItem) -> Unit,
    onDecrement: (CartItem) -> Unit,
    onRemove: (CartItem) -> Unit,
    onClear: () -> Unit,
    onContinue: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar producto de cualquier colegio…") },
            singleLine = true
        )
        if (query.isNotBlank() && state is CatalogUiState.Ready) {
            val results = remember(query, state) {
                state.colegios.flatMap { it.productos }
                    .filter { it.nombre.lowercase().contains(query.lowercase()) }
                    .take(30)
            }
            LazyColumn(Modifier.heightIn(max = 220.dp).padding(top = 8.dp)) {
                items(results, key = { it.idproducto }) { producto ->
                    ListItem(
                        headlineContent = { Text(producto.nombre, maxLines = 1) },
                        supportingContent = { Text(Utils.formatPrice(producto.precio)) },
                        modifier = Modifier.clickableItem { onAdd(producto) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Ticket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyColumn(Modifier.weight(1f).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ticket, key = { it.producto.idproducto }) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = BackgroundPaperRaised)) {
                    Row(Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.producto.nombre, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                            Text(Utils.formatPrice(item.producto.precio), style = MaterialTheme.typography.bodySmall, color = AccentThread)
                        }
                        IconButton(onClick = { onDecrement(item) }) { Icon(Icons.Default.Remove, contentDescription = null) }
                        Text(item.cantidad.toString())
                        IconButton(onClick = { onIncrement(item) }) { Icon(Icons.Default.Add, contentDescription = null) }
                    }
                }
            }
        }

        if (ticket.isNotEmpty()) {
            TextButton(onClick = onClear) { Text("Vaciar ticket") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", fontWeight = FontWeight.Bold)
            Text(Utils.formatPrice(total), fontWeight = FontWeight.Bold, color = AccentThread)
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onContinue,
            enabled = ticket.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentThread)
        ) { Text("Continuar a pago") }
    }
}

@Composable
private fun PagoStep(
    total: Int,
    metodoPago: String?,
    onMetodoChange: (String) -> Unit,
    montoRecibido: String,
    onMontoChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val montoNum = montoRecibido.toIntOrNull()
    val vuelto = if (montoNum != null) montoNum - total else null
    val suggestions = remember(total) {
        (listOf(total) + listOf(1000, 2000, 5000, 10000, 20000).map { r -> ceil(total.toDouble() / r).toInt() * r })
            .distinct().sorted().take(5)
    }
    val puedeConfirmar = when (metodoPago) {
        "efectivo" -> montoNum != null && montoNum >= total
        "tarjeta", "transferencia" -> true
        else -> false
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Total a cobrar", style = MaterialTheme.typography.labelLarge)
        Text(Utils.formatPrice(total), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = AccentThread)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            METODOS.forEach { m ->
                FilterChip(
                    selected = metodoPago == m,
                    onClick = { if (m != "webpay") onMetodoChange(m) },
                    enabled = m != "webpay",
                    label = { Text(if (m == "webpay") "Webpay (próximamente)" else m.replaceFirstChar { it.uppercase() }) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        when (metodoPago) {
            "efectivo" -> {
                OutlinedTextField(
                    value = montoRecibido,
                    onValueChange = onMontoChange,
                    label = { Text("Monto recibido") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    suggestions.forEach { s ->
                        AssistChip(onClick = { onMontoChange(s.toString()) }, label = { Text(Utils.formatPrice(s)) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                vuelto?.let {
                    Text(
                        "Vuelto: ${Utils.formatPrice(max(it, 0))}",
                        color = if (it < 0) MaterialTheme.colorScheme.error else SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            "tarjeta" -> Text("Se cobra en el POS físico del local; este sistema solo registra el método.", style = MaterialTheme.typography.bodySmall)
            "transferencia" -> Text("Verifica que la transferencia haya llegado antes de confirmar.", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.weight(1f))
        Row {
            TextButton(onClick = onBack) { Text("Volver al ticket") }
            Spacer(Modifier.weight(1f))
            Button(onClick = onConfirm, enabled = puedeConfirmar, colors = ButtonDefaults.buttonColors(containerColor = AccentThread)) {
                Text("Confirmar venta")
            }
        }
    }
}

@Composable
private fun ReciboStep(pedido: Pedido, warning: String?, onNuevaVenta: () -> Unit) {
    val neto = (pedido.total / 1.19).roundToInt()
    val iva = pedido.total - neto
    val fecha = remember(pedido.fecha) { Date(pedido.fecha) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        warning?.let {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(it, Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(10.dp))
        }
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundPaperRaised), modifier = Modifier.weight(1f)) {
            LazyColumn(Modifier.padding(16.dp)) {
                item {
                    Text("Confecciones Villa Acero", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("WhatsApp: +56 9 2068 0021", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("BOLETA", fontWeight = FontWeight.Bold)
                    Text("Código: ${pedido.codigoRetiro}")
                    Text("Fecha: ${SimpleDateFormat("dd-MM-yyyy", Locale("es", "CL")).format(fecha)}")
                    Text("Hora: ${SimpleDateFormat("HH:mm", Locale("es", "CL")).format(fecha)}")
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("ARTÍCULOS", fontWeight = FontWeight.SemiBold)
                }
                items(pedido.items) { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.nombre} (${item.talla})", style = MaterialTheme.typography.bodySmall, maxLines = 1, modifier = Modifier.weight(1f))
                        Text(Utils.formatPrice(item.precio * item.cantidad), style = MaterialTheme.typography.bodySmall)
                    }
                }
                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Neto"); Text(Utils.formatPrice(neto)) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("IVA"); Text(Utils.formatPrice(iva)) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(Utils.formatPrice(pedido.total), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = AccentThread)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Método: ${pedido.metodoPago}")
                    if (pedido.metodoPago == "efectivo") {
                        Text("Recibido: ${Utils.formatPrice(pedido.montoRecibido ?: 0)}")
                        Text("Vuelto: ${Utils.formatPrice(max(pedido.vuelto ?: 0, 0))}")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("¡Gracias por su compra!", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onNuevaVenta, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentThread)) {
            Text("Nueva venta")
        }
    }
}

private fun Modifier.clickableItem(onClick: () -> Unit): Modifier = this.clickable(onClick = onClick)

package com.example.aplicaciontienda.ui.screens.cart

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.aplicaciontienda.CartItem
import com.example.aplicaciontienda.CartItemPedido
import com.example.aplicaciontienda.CartManager
import com.example.aplicaciontienda.LocalOrdersManager
import com.example.aplicaciontienda.Pedido
import com.example.aplicaciontienda.R
import com.example.aplicaciontienda.Utils
import com.example.aplicaciontienda.data.firebase.BehaviorTracker
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import com.example.aplicaciontienda.ui.theme.SuccessGreen
import com.google.firebase.database.FirebaseDatabase

/** Equivalente a Cart.jsx / CartActivity: carrito, asistente de personalización y checkout WhatsApp/Webpay. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(onBack: () -> Unit, onContinueShopping: () -> Unit, onNavigateToWebpay: (orderCode: String, amount: Int) -> Unit = { _, _ -> }) {
    val context = LocalContext.current
    var itemsVersion by remember { mutableStateOf(0) } // fuerza recomposición tras mutar CartManager
    val items = CartManager.getItems()
    var showAssistant by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<((Int, String) -> Unit)?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showFaq by remember { mutableStateOf(false) }
    var successCode by remember { mutableStateOf<String?>(null) }
    var orderConfirmed by remember { mutableStateOf(false) }

    fun refresh() { itemsVersion++ }

    // Equivalente al tracking de "abandoned_carts" de Cart.jsx: si el usuario sale con ítems
    // en el carrito sin haber confirmado un pedido, se registra como carrito abandonado.
    DisposableEffect(Unit) {
        onDispose {
            if (!orderConfirmed) {
                val remaining = CartManager.getItems()
                if (remaining.isNotEmpty()) {
                    val total = CartManager.getTotal()
                    val colegios = remaining.map { it.producto.colegio }.filter { it.isNotEmpty() }.distinct()
                    try {
                        val entry = mapOf(
                            "items" to remaining.map {
                                mapOf(
                                    "nombre" to it.producto.nombre,
                                    "precio" to it.producto.precio,
                                    "cantidad" to it.cantidad,
                                    "colegio" to it.producto.colegio
                                )
                            },
                            "total" to total,
                            "abandonedAt" to System.currentTimeMillis()
                        )
                        val key = "${System.currentTimeMillis()}_${(1000..9999).random()}"
                        FirebaseDatabase.getInstance().getReference("abandoned_carts").child(key).setValue(entry)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    BehaviorTracker.registrarCarritoAbandonado(mapOf("total" to total, "colegios" to colegios))
                }
            }
        }
    }

    fun guardarPedido(orderCode: String, extraCharge: Int, customization: String, metodoWebpay: Boolean) {
        val itemsSnapshot = CartManager.getItems().toList()
        val total = CartManager.getTotal()
        LocalOrdersManager.saveOrder(context, orderCode)
        try {
            val itemPedido = itemsSnapshot.map {
                CartItemPedido(it.producto.nombre, it.producto.talla, it.producto.precio, it.cantidad, it.producto.colegio)
            }
            val pedido = Pedido(
                id = orderCode,
                codigoRetiro = orderCode,
                items = itemPedido,
                total = total,
                extraCharge = extraCharge,
                customization = customization,
                estado = 1,
                metodoPago = if (metodoWebpay) "webpay" else null,
                pagado = if (metodoWebpay) false else null
            )
            FirebaseDatabase.getInstance().getReference("pedidos").child(orderCode).setValue(pedido)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cart_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.cart_empty), style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onContinueShopping) { Text("Seguir comprando") }
                }
            }
            return@Scaffold
        }

        Column(Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                items(items, key = { it.producto.idproducto }) { item ->
                    CartLineItem(
                        item = item,
                        onIncrement = { CartManager.updateQuantity(item, 1); refresh() },
                        onDecrement = { CartManager.updateQuantity(item, -1); refresh() },
                        onRemove = { CartManager.removeItem(item); refresh() }
                    )
                }
            }

            HorizontalDivider()
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.cart_subtotal))
                    Text(Utils.formatPrice(CartManager.getTotal()), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.cart_total), fontWeight = FontWeight.Bold)
                    Text(Utils.formatPrice(CartManager.getTotal()), fontWeight = FontWeight.Bold, color = AccentThread)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ver preguntas frecuentes y ubicación",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp).clickable { showFaq = true }
                )
                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {
                        pendingAction = { extra, cust ->
                            val code = Utils.generateOrderCode()
                            val total = CartManager.getTotal()
                            val totalFinal = total + extra
                            val sb = StringBuilder("¡Hola! Me gustaría realizar el siguiente pedido:\n\n")
                            sb.append("CÓDIGO DE RETIRO: $code\n")
                            sb.append("------------------------------\n")
                            items.forEach { item ->
                                sb.append("• ${item.producto.nombre}\n")
                                sb.append("  Cantidad: ${item.cantidad}\n")
                                if (item.producto.colegio.isNotEmpty()) sb.append("  Colegio: ${item.producto.colegio}\n")
                                sb.append("\n")
                            }
                            if (cust.isNotEmpty()) {
                                sb.append("Detalles de Ajuste: $cust\n")
                                sb.append("Cargo extra: ${Utils.formatPrice(extra)}\n\n")
                            }
                            sb.append("Total final: ${Utils.formatPrice(totalFinal)}\n\n")
                            sb.append("*Nota: Tengo 3 días hábiles para cualquier cambio de opinión sobre los ajustes.*")

                            guardarPedido(code, extra, cust, metodoWebpay = false)
                            items.forEach { BehaviorTracker.registrarCompra(it.producto.nombre, it.producto.precio, mapOf("colegio" to it.producto.colegio, "cantidad" to it.cantidad)) }
                            Utils.openWhatsApp(context, "+56920680021", sb.toString())
                            orderConfirmed = true
                            CartManager.clear()
                            refresh()
                            successCode = code
                        }
                        showAssistant = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentThread)
                ) { Text(stringResource(R.string.send_via_whatsapp), fontWeight = FontWeight.SemiBold) }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        pendingAction = { extra, cust ->
                            val code = Utils.generateOrderCode()
                            val totalFinal = CartManager.getTotal() + extra
                            guardarPedido(code, extra, cust, metodoWebpay = true)
                            items.forEach { BehaviorTracker.registrarCompra(it.producto.nombre, it.producto.precio, mapOf("colegio" to it.producto.colegio, "cantidad" to it.cantidad)) }
                            orderConfirmed = true
                            CartManager.clear()
                            refresh()
                            onNavigateToWebpay(code, totalFinal)
                        }
                        showAssistant = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text(stringResource(R.string.cart_checkout_webpay)) }

                Spacer(Modifier.height(10.dp))
                TextButton(onClick = { showClearConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Vaciar carrito")
                }
            }
        }
    }

    if (showAssistant) {
        CheckoutAssistantDialog(
            onDismiss = { showAssistant = false },
            onComplete = { extra, cust ->
                showAssistant = false
                pendingAction?.invoke(extra, cust)
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.clear_cart_title)) },
            text = { Text(stringResource(R.string.clear_cart_message)) },
            confirmButton = {
                TextButton(onClick = { CartManager.clear(); refresh(); showClearConfirm = false }) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text(stringResource(R.string.no)) } }
        )
    }

    if (showFaq) {
        FaqDialog(onDismiss = { showFaq = false })
    }

    successCode?.let { code ->
        OrderSuccessDialog(orderCode = code, onDismiss = { successCode = null; onContinueShopping() })
    }
}

@Composable
private fun CartLineItem(item: CartItem, onIncrement: () -> Unit, onDecrement: () -> Unit, onRemove: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = BackgroundPaperRaised)) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.producto.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2)
                Text(Utils.formatPrice(item.producto.precio), style = MaterialTheme.typography.bodySmall, color = AccentThread)
            }
            IconButton(onClick = onDecrement) { Icon(Icons.Default.Remove, contentDescription = "Menos") }
            Text(item.cantidad.toString(), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onIncrement) { Icon(Icons.Default.Add, contentDescription = "Más") }
        }
    }
}

@Composable
private fun FaqDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.faq_title)) },
        text = {
            LazyColumn(Modifier.heightIn(max = 400.dp)) {
                val faqIds = listOf(
                    R.string.faq_q1 to R.string.faq_a1, R.string.faq_q2 to R.string.faq_a2,
                    R.string.faq_q10 to R.string.faq_a10, R.string.faq_q16 to R.string.faq_a16,
                    R.string.faq_q17 to R.string.faq_a17
                )
                items(faqIds) { (q, a) ->
                    Column(Modifier.padding(vertical = 6.dp)) {
                        Text(stringResource(q), fontWeight = FontWeight.SemiBold)
                        Text(stringResource(a), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { Utils.openGoogleMaps(context, "Tienda Villa Acero, Hualpén"); onDismiss() }) { Text("Ver ubicación") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun OrderSuccessDialog(orderCode: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var rated by remember { mutableStateOf(false) }
    val qrBitmap = remember(orderCode) { Utils.generateQRCode(orderCode, 500) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.order_success), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.order_code_is, orderCode), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Código de Retiro", orderCode))
                }) { Text(stringResource(R.string.copy_code)) }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.show_qr_to_cashier), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                qrBitmap?.let {
                    Image(bitmap = it.asImageBitmap(), contentDescription = "QR", modifier = Modifier.size(180.dp))
                }
                Spacer(Modifier.height(16.dp))
                if (!rated) {
                    Text(stringResource(R.string.experience_rating), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        for (i in 1..5) {
                            TextButton(onClick = { rated = true }) { Text("⭐") }
                        }
                    }
                } else {
                    Text("¡Gracias por tu preferencia! ❤️", color = SuccessGreen)
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.understood)) }
            }
        }
    }
}

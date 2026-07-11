package com.example.aplicaciontienda.ui.screens.webpay

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.SuccessGreen

/** Equivalente a PagoResultado.jsx: pantalla de resultado tras volver del WebView de Webpay. */
@Composable
fun PagoResultadoScreen(orderCode: String, estado: String, onVerPedido: () -> Unit, onVolverInicio: () -> Unit) {
    val (titulo, mensaje, color) = when (estado) {
        "ok" -> Triple("¡Pago exitoso! 🎉", "Tu pedido #$orderCode fue pagado correctamente.", SuccessGreen)
        "anulado" -> Triple("Pago cancelado", "Cancelaste el pago antes de completarlo. Tu pedido #$orderCode sigue pendiente.", MaterialTheme.colorScheme.error)
        "rechazado" -> Triple("Pago rechazado", "El banco rechazó la transacción de tu pedido #$orderCode. Puedes intentar de nuevo.", MaterialTheme.colorScheme.error)
        else -> Triple("Ocurrió un problema", "No pudimos confirmar el estado del pago de tu pedido #$orderCode. Consulta en seguimiento en unos minutos.", MaterialTheme.colorScheme.error)
    }

    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.height(12.dp))
        Text(mensaje, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onVerPedido,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentThread)
        ) { Text("Ver seguimiento del pedido") }
        Spacer(Modifier.height(10.dp))
        TextButton(onClick = onVolverInicio) { Text("Volver al inicio") }
    }
}

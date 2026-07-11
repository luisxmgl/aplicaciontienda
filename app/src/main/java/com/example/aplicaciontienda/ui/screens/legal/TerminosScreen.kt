package com.example.aplicaciontienda.ui.screens.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Equivalente a TerminosCondiciones.jsx: mismas políticas ya usadas en el FAQ de la tienda (strings.xml). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminosScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Términos y Condiciones") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Seccion("Pedidos y pago", "Los pedidos se confirman al recibir el pago (Webpay, o efectivo/transferencia/tarjeta en tienda). Cada pedido genera un código único de retiro que debe presentarse en tienda.")
            Seccion("Personalización y cambios", "Tienes hasta 3 días hábiles para modificar detalles de personalización (ajustes de medida, bordados) antes de que el pedido entre a producción final.")
            Seccion("Retiro", "El retiro es exclusivamente en nuestra tienda física en Hualpén, presentando el código de retiro a la cajera. El código es de un solo uso.")
            Seccion("Anulaciones", "Los productos estándar pueden anularse antes del retiro. Los productos personalizados no admiten anulación una vez pasado el plazo de 3 días hábiles.")
            Seccion("Garantía", "Cubrimos fallas de fábrica (costuras o tela defectuosa) presentando el comprobante de compra. No cubre daños por mal uso o lavado inadecuado.")
            Seccion("Horario de atención", "Lunes a viernes de 09:00 a 18:30, sábados de 10:00 a 14:00 (excepto festivos).")
            Seccion("Contacto", "Ante cualquier duda sobre estos términos, escríbenos por WhatsApp o desde la sección Contacto de la app.")
        }
    }
}

@Composable
private fun Seccion(titulo: String, texto: String) {
    Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    Text(texto, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(16.dp))
}

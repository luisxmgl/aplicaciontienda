package com.example.aplicaciontienda.ui.screens.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.aplicaciontienda.Utils
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import com.example.aplicaciontienda.ui.theme.PrimaryInk
import com.example.aplicaciontienda.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

private data class ChatBubble(val text: String, val isBot: Boolean)
private data class ChatOption(val text: String, val onSelected: () -> Unit)

/**
 * Asistente de personalización del carrito (chatbot de pasos), mismo árbol de decisión que
 * el chatbot original de CartActivity / CheckoutAssistant.jsx de la web:
 * inicio -> ajustes -> bordado -> bordado_lugar -> talla -> regalo -> plazo -> fin
 */
@Composable
fun CheckoutAssistantDialog(onDismiss: () -> Unit, onComplete: (extraCharge: Int, customization: String) -> Unit) {
    val mensajes = remember { mutableStateListOf<ChatBubble>() }
    var opciones by remember { mutableStateOf<List<ChatOption>>(emptyList()) }
    var extraCharge by remember { mutableStateOf(0) }
    val notas = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    var typing by remember { mutableStateOf(false) }

    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) listState.animateScrollToItem(mensajes.size - 1)
    }

    fun addBot(text: String) {
        mensajes.add(ChatBubble(text, isBot = true))
    }

    fun addUser(text: String) {
        mensajes.add(ChatBubble(text, isBot = false))
    }

    fun stepFinished() {
        opciones = emptyList()
        val finalCustomization = notas.joinToString(", ")
        val summary = if (extraCharge > 0)
            "¡Excelente! He configurado tus ajustes: $finalCustomization.\n\nEl total se actualizará con un cargo de ${Utils.formatPrice(extraCharge)} por el trabajo adicional."
        else "Entendido. Procesaremos tu pedido con las medidas estándar de fábrica."
        addBot(summary)
        addBot("¿Deseas finalizar el pedido ahora?")
        opciones = listOf(
            ChatOption("✅ SÍ, FINALIZAR PEDIDO") { onComplete(extraCharge, finalCustomization) }
        )
    }

    fun stepDeadline() {
        addBot("Para coordinar con el taller, ¿cuándo necesitas tener tu pedido en mano?")
        opciones = listOf(
            ChatOption("📅 Lo antes posible") { notas.add("Prioridad normal"); stepFinished() },
            ChatOption("🎁 Es para un regalo (Esta semana)") { notas.add("Urgencia: Para esta semana"); stepFinished() },
            ChatOption("🗓️ No tengo prisa (Próxima semana)") { notas.add("Sin urgencia"); stepFinished() }
        )
    }

    fun stepGiftWrapping() {
        addBot("¿Te gustaría que envolvamos tu pedido para regalo? 🎀 (Sin costo adicional)")
        opciones = listOf(
            ChatOption("🎁 Sí, por favor") { notas.add("Envolver para regalo"); stepDeadline() },
            ChatOption("❌ No es necesario") { stepDeadline() }
        )
    }

    fun stepSizeConfirmation() {
        addBot("Una pregunta importante: ¿Estás seguro de la talla elegida? (Si tienes dudas, podemos asesorarte al retirar)")
        opciones = listOf(
            ChatOption("✅ Sí, la talla es correcta") { notas.add("Talla confirmada"); stepGiftWrapping() },
            ChatOption("❓ No estoy seguro, prefiero asesoría") { notas.add("Requiere asesoría de talla en tienda"); stepGiftWrapping() }
        )
    }

    fun stepEmbroideryPlacement() {
        addBot("¿En qué parte de la prenda prefieres el bordado?")
        opciones = listOf(
            ChatOption("Pecho Izquierdo") { notas.add("Bordado en pecho izquierdo"); stepSizeConfirmation() },
            ChatOption("Pecho Derecho") { notas.add("Bordado en pecho derecho"); stepSizeConfirmation() },
            ChatOption("En la Espalda / Cuello") { notas.add("Bordado en espalda/cuello"); stepSizeConfirmation() }
        )
    }

    fun stepEmbroidery() {
        addBot("¿Te gustaría agregar el nombre bordado del alumno? 🧵 (Valor: $1.000)")
        opciones = listOf(
            ChatOption("🧵 Sí, agregar bordado") { extraCharge += 1000; stepEmbroideryPlacement() },
            ChatOption("No, sin bordado") { stepSizeConfirmation() }
        )
    }

    fun stepAdjustments() {
        addBot("¿Necesitas que hagamos algún ajuste de medida? 👖 (Cada ajuste tiene un valor de $1.000)")
        opciones = listOf(
            ChatOption("👖 Ajuste de Basta / Largo") { extraCharge += 1000; notas.add("Ajuste de basta"); stepEmbroidery() },
            ChatOption("👖 Corte más Ajustado (Slim)") { extraCharge += 1000; notas.add("Corte Slim"); stepEmbroidery() },
            ChatOption("🧥 Ajustar largo de Mangas") { extraCharge += 1000; notas.add("Ajuste de mangas"); stepEmbroidery() },
            ChatOption("✨ Sin ajustes, solo confirmar detalles") { stepEmbroidery() }
        )
    }

    LaunchedEffect(Unit) {
        delay(300)
        addBot("¡Hola! Soy tu Asistente Villa Acero. 😊\n\nEstoy aquí para que tu pedido sea perfecto. ¿Te gustaría personalizar tu ropa con ajustes o bordados?")
        opciones = listOf(
            ChatOption("🌟 Sí, me interesa") { stepAdjustments() },
            ChatOption("📏 Solo confirmar tallas/regalo") { stepSizeConfirmation() },
            ChatOption("🚀 No, comprar estándar") { onComplete(0, "") }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().heightIn(max = 560.dp).padding(16.dp)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mensajes) { bubble -> ChatBubbleView(bubble) }
                }
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    opciones.forEach { opcion ->
                        val isConfirm = opcion.text.contains("FINALIZAR")
                        Button(
                            onClick = {
                                if (!isConfirm) addUser(opcion.text)
                                opciones = emptyList()
                                opcion.onSelected()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConfirm) SuccessGreen else Color.White,
                                contentColor = if (isConfirm) Color.White else PrimaryInk
                            ),
                            border = if (isConfirm) null else androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryInk)
                        ) { Text(opcion.text) }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleView(bubble: ChatBubble) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (bubble.isBot) Arrangement.Start else Arrangement.End) {
        Surface(
            color = if (bubble.isBot) BackgroundPaperRaised else PrimaryInk,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = bubble.text,
                color = if (bubble.isBot) MaterialTheme.colorScheme.onSurface else Color.White,
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

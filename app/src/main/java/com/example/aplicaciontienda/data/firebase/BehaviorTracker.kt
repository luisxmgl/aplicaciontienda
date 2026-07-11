package com.example.aplicaciontienda.data.firebase

import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Equivalente a src/utils/BehaviorTracker.js: escribe eventos de analítica a `behavior_events`
 * en Firebase RTDB, mismo shape de eventos que la web (mismo nodo compartido).
 */
object BehaviorTracker {
    private val sessionId = "session_${System.currentTimeMillis()}_${Random.nextInt(100000, 999999)}"

    private fun envelope(tipo: String, extra: Map<String, Any?>): Map<String, Any?> {
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return mapOf(
            "tipo" to tipo,
            "sessionId" to sessionId,
            "timestamp" to System.currentTimeMillis(),
            "fecha" to fecha
        ) + extra
    }

    private fun push(tipo: String, extra: Map<String, Any?>) {
        try {
            FirebaseDatabase.getInstance().getReference("behavior_events").push().setValue(envelope(tipo, extra))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun registrarVistaProducto(producto: String, precio: Int, extra: Map<String, Any?> = emptyMap()) {
        push("product_view", mapOf("producto" to producto, "precio" to precio) + extra)
    }

    fun registrarAgregarCarrito(producto: String, cantidad: Int, extra: Map<String, Any?> = emptyMap()) {
        push("cart_add", mapOf("producto" to producto, "cantidad" to cantidad) + extra)
    }

    fun registrarCompra(producto: String, precio: Int, extra: Map<String, Any?> = emptyMap()) {
        push("product_purchased", mapOf("producto" to producto, "precio" to precio) + extra)
    }

    fun registrarVistaPagina(pagina: String, extra: Map<String, Any?> = emptyMap()) {
        push("page_view", mapOf("pagina" to pagina) + extra)
    }

    fun registrarCarritoAbandonado(extra: Map<String, Any?> = emptyMap()) {
        push("cart_abandoned", extra)
    }
}

package com.example.aplicaciontienda.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.aplicaciontienda.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class AdminBadgeCounts(val unreadMessages: Int = 0, val newOrders: Int = 0)

private const val PREFS_NAME = "NotificationsPrefs"
private const val KEY_ORDERS_SEEN_TS = "admin_orders_seen_ts"

private fun getOrdersSeenTs(context: Context): Long {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    if (!prefs.contains(KEY_ORDERS_SEEN_TS)) {
        // Primera vez: no marcar como "nuevos" todos los pedidos históricos.
        prefs.edit().putLong(KEY_ORDERS_SEEN_TS, System.currentTimeMillis()).apply()
    }
    return prefs.getLong(KEY_ORDERS_SEEN_TS, System.currentTimeMillis())
}

/** Se llama al entrar a "Gestión de Pedidos" para que el círculo de notificación desaparezca. */
fun markOrdersSeen(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putLong(KEY_ORDERS_SEEN_TS, System.currentTimeMillis()).apply()
}

/**
 * Contadores en tiempo real para los círculos de notificación del panel admin:
 * - Mensajes: total de mensajes de clientes sin leer (nodo Firebase "chats", campo "read").
 *   Desaparece solo, en vivo, apenas se abre esa conversación (ChatScreen marca read=true).
 * - Gestión: pedidos nuevos desde la última vez que el admin entró a "Gestión de Pedidos".
 */
@Composable
fun rememberAdminBadgeCounts(esAdmin: Boolean): State<AdminBadgeCounts> {
    val context = LocalContext.current
    val state = remember { mutableStateOf(AdminBadgeCounts()) }

    DisposableEffect(esAdmin) {
        if (!esAdmin) return@DisposableEffect onDispose { }

        val chatsRef = FirebaseDatabase.getInstance().getReference("chats")
        val chatsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (chatSnap in snapshot.children) {
                    for (msgSnap in chatSnap.children) {
                        val msg = msgSnap.getValue(Message::class.java) ?: continue
                        if (msg.senderId != "admin" && !msg.read) count++
                    }
                }
                state.value = state.value.copy(unreadMessages = count)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        chatsRef.addValueEventListener(chatsListener)

        val ordersRef = FirebaseDatabase.getInstance().getReference("pedidos")
        val ordersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val seenTs = getOrdersSeenTs(context)
                val count = snapshot.children.count { child ->
                    (child.child("fecha").getValue(Long::class.java) ?: 0L) > seenTs
                }
                state.value = state.value.copy(newOrders = count)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ordersRef.addValueEventListener(ordersListener)

        onDispose {
            chatsRef.removeEventListener(chatsListener)
            ordersRef.removeEventListener(ordersListener)
        }
    }
    return state
}

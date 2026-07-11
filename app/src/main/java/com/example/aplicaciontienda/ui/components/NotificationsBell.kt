package com.example.aplicaciontienda.ui.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.LocalOrdersManager
import com.example.aplicaciontienda.Message
import com.example.aplicaciontienda.Pedido
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class AppNotification(val id: String, val message: String, val route: String)

private fun prefs(context: Context) = context.getSharedPreferences("NotificationsPrefs", Context.MODE_PRIVATE)
private fun seenChat(context: Context, chatId: String) = prefs(context).getLong("opened_chat_$chatId", 0L)
private fun markChatSeen(context: Context, chatId: String, ts: Long) = prefs(context).edit().putLong("opened_chat_$chatId", ts).apply()
private fun seenOrderEstado(context: Context, code: String) = prefs(context).getInt("seen_order_estado_$code", 1)
private fun markOrderSeen(context: Context, code: String, estado: Int) = prefs(context).edit().putInt("seen_order_estado_$code", estado).apply()

/**
 * Agregador de notificaciones para invitados: mensajes nuevos del chat propio + cambios de estado
 * en pedidos guardados localmente. Equivalente simplificado de src/hooks/useNotifications.js.
 */
@Composable
fun rememberGuestNotifications(guestId: String): State<List<AppNotification>> {
    val context = LocalContext.current
    val state = remember { mutableStateOf<List<AppNotification>>(emptyList()) }

    DisposableEffect(guestId) {
        val notifications = mutableStateMapOf<String, AppNotification>()

        val chatRef = FirebaseDatabase.getInstance().getReference("chats").child(guestId)
        val chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var lastFromStore: Message? = null
                for (child in snapshot.children) {
                    val msg = child.getValue(Message::class.java) ?: continue
                    if (msg.senderId != guestId) lastFromStore = msg
                }
                val key = "chat"
                if (lastFromStore != null && lastFromStore.timestamp > seenChat(context, guestId)) {
                    notifications[key] = AppNotification(key, "Villa Acero te respondió", "chat?esAdmin=false&targetId=")
                } else {
                    notifications.remove(key)
                }
                state.value = notifications.values.toList()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        chatRef.addValueEventListener(chatListener)

        val pedidosRef = FirebaseDatabase.getInstance().getReference("pedidos")
        val pedidosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val misCodigos = LocalOrdersManager.getOrders(context).toSet()
                for (child in snapshot.children) {
                    val pedido = child.getValue(Pedido::class.java) ?: continue
                    if (pedido.codigoRetiro !in misCodigos) continue
                    val key = "order_${pedido.codigoRetiro}"
                    if (pedido.estado > seenOrderEstado(context, pedido.codigoRetiro)) {
                        notifications[key] = AppNotification(key, "Tu pedido ${pedido.codigoRetiro} cambió de estado", "seguimiento?code=${pedido.codigoRetiro}")
                    } else {
                        notifications.remove(key)
                    }
                }
                state.value = notifications.values.toList()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        pedidosRef.addValueEventListener(pedidosListener)

        onDispose {
            chatRef.removeEventListener(chatListener)
            pedidosRef.removeEventListener(pedidosListener)
        }
    }
    return state
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBellButton(guestId: String, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val notifications by rememberGuestNotifications(guestId)
    var showSheet by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showSheet = true }) {
            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
        }
        if (notifications.isNotEmpty()) {
            Badge(containerColor = AccentThread, modifier = Modifier.align(Alignment.TopEnd)) {
                Text(if (notifications.size > 9) "9+" else notifications.size.toString())
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("Notificaciones", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                if (notifications.isEmpty()) {
                    Text("No tienes notificaciones nuevas.")
                } else {
                    LazyColumn(Modifier.heightIn(max = 320.dp)) {
                        items(notifications, key = { it.id }) { n ->
                            ListItem(
                                headlineContent = { Text(n.message) },
                                modifier = Modifier.clickableNotification {
                                    if (n.id == "chat") markChatSeen(context, guestId, System.currentTimeMillis())
                                    if (n.id.startsWith("order_")) {
                                        val code = n.id.removePrefix("order_")
                                        markOrderSeen(context, code, 4)
                                    }
                                    showSheet = false
                                    onNavigate(n.route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.clickableNotification(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

package com.example.aplicaciontienda.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.Message
import com.example.aplicaciontienda.PresenceManager
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import com.example.aplicaciontienda.ui.theme.PrimaryInk
import com.example.aplicaciontienda.ui.theme.SuccessGreen
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/** Marca como leídos todos los mensajes de clientes pendientes en todas las conversaciones. */
private fun marcarTodoComoLeido() {
    val chatsRef = FirebaseDatabase.getInstance().getReference("chats")
    chatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (chatSnap in snapshot.children) {
                for (msgSnap in chatSnap.children) {
                    val msg = msgSnap.getValue(Message::class.java) ?: continue
                    if (msg.senderId != "admin" && !msg.read) {
                        msgSnap.ref.child("read").setValue(true)
                    }
                }
            }
        }
        override fun onCancelled(error: DatabaseError) {}
    })
}

private data class ChatSummary(
    val chatId: String,
    val lastMessage: String,
    val hasUnread: Boolean,
    val timestamp: Long,
    val lastSeen: Long = 0L
)

/** Equivalente a AdminChatList.jsx / AdminChatListActivity: lista de conversaciones ordenadas por actividad. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChatListScreen(onBack: () -> Unit, onOpenChat: (String) -> Unit) {
    val chats = remember { mutableStateListOf<ChatSummary>() }

    LaunchedEffect(Unit) { PresenceManager.updateLastSeen("admin") }

    DisposableEffect(Unit) {
        val chatsRef = FirebaseDatabase.getInstance().getReference("chats")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val summaries = snapshot.children.mapNotNull { postSnapshot ->
                    val chatId = postSnapshot.key ?: return@mapNotNull null
                    var lastMsgText = "Imagen"
                    var lastTimestamp = 0L
                    var hasUnread = false
                    for (msgSnap in postSnapshot.children) {
                        val msg = msgSnap.getValue(Message::class.java) ?: continue
                        if (msg.timestamp > lastTimestamp) {
                            lastTimestamp = msg.timestamp
                            lastMsgText = msg.text.ifEmpty { "📷 Imagen" }
                        }
                        if (msg.senderId != "admin" && !msg.read) hasUnread = true
                    }
                    ChatSummary(chatId, lastMsgText, hasUnread, lastTimestamp)
                }

                FirebaseDatabase.getInstance().getReference("presence")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(presenceSnapshot: DataSnapshot) {
                            val updated = summaries.map {
                                it.copy(lastSeen = PresenceManager.extractLastSeen(presenceSnapshot.child(it.chatId)))
                            }.sortedByDescending { it.timestamp }
                            chats.clear()
                            chats.addAll(updated)
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        chatsRef.addValueEventListener(listener)
        onDispose { chatsRef.removeEventListener(listener) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mensajes Recibidos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
                actions = {
                    IconButton(onClick = { marcarTodoComoLeido() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Marcar todo como leído")
                    }
                }
            )
        }
    ) { padding ->
        if (chats.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sin conversaciones todavía.") }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                items(chats, key = { it.chatId }) { chat -> ChatSummaryRow(chat, onClick = { onOpenChat(chat.chatId) }) }
            }
        }
    }
}

@Composable
private fun ChatSummaryRow(chat: ChatSummary, onClick: () -> Unit) {
    val online = System.currentTimeMillis() - chat.lastSeen < 60000
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = BackgroundPaperRaised)) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    Modifier.size(40.dp).background(PrimaryInk, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(chat.chatId.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                if (online) {
                    Box(Modifier.size(10.dp).background(SuccessGreen, CircleShape))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(chat.chatId, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text("Cliente: ${chat.lastMessage}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            if (chat.hasUnread) {
                Box(Modifier.size(10.dp).background(AccentThread, CircleShape))
            }
        }
    }
}

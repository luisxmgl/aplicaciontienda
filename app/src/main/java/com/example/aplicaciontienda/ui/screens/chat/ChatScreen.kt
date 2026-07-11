package com.example.aplicaciontienda.ui.screens.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.aplicaciontienda.Message
import com.example.aplicaciontienda.PresenceManager
import com.example.aplicaciontienda.Utils
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import com.example.aplicaciontienda.ui.theme.PrimaryInk
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream

/** Comprime la imagen (máx. 1024px, JPEG calidad 70) y la codifica en base64 dentro de RTDB, igual que la web (sin Storage). */
private fun encodeImageToDataUrl(context: Context, uri: Uri): String? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val original = BitmapFactory.decodeStream(input)
        input.close()
        val maxDim = 1024
        val scale = minOf(1f, maxDim.toFloat() / maxOf(original.width, original.height))
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(original, (original.width * scale).toInt(), (original.height * scale).toInt(), true)
        } else original
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, out)
        "data:image/jpeg;base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun decodeDataUrlToBitmap(dataUrl: String): Bitmap? {
    return try {
        val base64 = dataUrl.substringAfter("base64,")
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}

/** Equivalente a Chat.jsx / ChatActivity, con imágenes base64 en RTDB (sin Firebase Storage) para paridad con la web. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(esAdmin: Boolean, targetId: String?, onBack: () -> Unit) {
    val context = LocalContext.current
    val currentUserId = remember { if (esAdmin) "admin" else Utils.getUniqueUserId(context) }
    val chatId = remember { if (esAdmin) (targetId ?: "") else currentUserId }
    val messages = remember { mutableStateListOf<Message>() }
    var texto by remember { mutableStateOf("") }
    var presenceText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val dataUrl = encodeImageToDataUrl(context, it)
            if (dataUrl != null) {
                val msg = Message(senderId = currentUserId, imageUrl = dataUrl, timestamp = System.currentTimeMillis())
                FirebaseDatabase.getInstance().getReference("chats").child(chatId).push().setValue(msg)
                PresenceManager.updateLastSeen(currentUserId)
            }
        }
    }

    LaunchedEffect(chatId) {
        if (chatId.isEmpty()) return@LaunchedEffect
        val dbRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (postSnapshot in snapshot.children) {
                    val message = postSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                        if (esAdmin && message.senderId != "admin" && !message.read) {
                            postSnapshot.ref.child("read").setValue(true)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    LaunchedEffect(chatId) {
        val targetPresenceId = if (esAdmin) chatId else "admin"
        if (targetPresenceId.isEmpty()) return@LaunchedEffect
        FirebaseDatabase.getInstance().getReference("presence").child(targetPresenceId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lastSeen = PresenceManager.extractLastSeen(snapshot)
                    presenceText = PresenceManager.getFormattedPresence(lastSeen)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Heartbeat de presencia mientras el chat está abierto (igual que el intervalo de 20s de la web);
    // al salir se marca offline, también igual que la web.
    DisposableEffect(currentUserId) {
        onDispose { PresenceManager.setOffline(currentUserId) }
    }
    LaunchedEffect(currentUserId) {
        while (true) {
            PresenceManager.updateLastSeen(currentUserId)
            delay(20000)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (esAdmin) "Chat con $chatId" else "Chat con Villa Acero")
                        if (presenceText.isNotEmpty()) Text(presenceText, style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
            )
        },
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { imageLauncher.launch("image/*") }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar")
                }
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje…") },
                    singleLine = true
                )
                IconButton(onClick = {
                    if (texto.isNotBlank()) {
                        val msg = Message(senderId = currentUserId, text = texto.trim(), timestamp = System.currentTimeMillis())
                        FirebaseDatabase.getInstance().getReference("chats").child(chatId).push().setValue(msg)
                        PresenceManager.updateLastSeen(currentUserId)
                        texto = ""
                    }
                }) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar") }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            items(messages) { message -> ChatMessageBubble(message, isMine = message.senderId == currentUserId) }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: Message, isMine: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start) {
        Surface(
            color = if (isMine) PrimaryInk else BackgroundPaperRaised,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            Box(Modifier.padding(10.dp)) {
                val imageUrl = message.imageUrl
                when {
                    imageUrl != null && imageUrl.startsWith("data:") -> {
                        val bitmap = remember(imageUrl) { decodeDataUrlToBitmap(imageUrl) }
                        bitmap?.let { Image(it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(180.dp)) }
                    }
                    imageUrl != null -> AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.size(180.dp))
                    else -> Text(
                        message.text,
                        color = if (isMine) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

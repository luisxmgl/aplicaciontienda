package com.example.aplicaciontienda

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var etMensaje: EditText
    private lateinit var btnEnviar: Button
    private lateinit var btnAdjuntar: ImageButton
    private lateinit var rvChat: RecyclerView
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    
    private lateinit var dbRef: DatabaseReference
    private var chatId: String = ""
    private var esAdmin: Boolean = false
    private var currentUserId: String = ""

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> uploadImage(uri) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        esAdmin = intent.getBooleanExtra("ES_ADMIN", false)
        currentUserId = if (esAdmin) "admin" else getUniqueUserId()
        val targetId = intent.getStringExtra("TARGET_ID") ?: "admin"
        
        // El chatId identifica la conversación. 
        // Si el invitado abre el chat, su ID es el chatId.
        // Si el admin abre un chat desde la lista, el ID del invitado es el chatId.
        chatId = if (esAdmin) targetId else currentUserId

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarChat)
        toolbar.title = if (esAdmin) "Chat con $chatId" else "Chat con Villa Acero"
        toolbar.setNavigationOnClickListener { finish() }

        etMensaje = findViewById(R.id.etMensaje)
        btnEnviar = findViewById(R.id.btnEnviar)
        btnAdjuntar = findViewById(R.id.btnAdjuntar)
        rvChat = findViewById(R.id.rvChat)

        adapter = ChatAdapter(messages, currentUserId)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = adapter

        // Referencia a los mensajes del chat específico
        dbRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId)
        
        // Si es invitado y viene del carrito, enviar mensaje automático
        val cartInfo = intent.getStringExtra("CART_INFO")
        if (!esAdmin && cartInfo != null) {
            sendMessage(cartInfo)
        }

        listenForMessages()

        btnEnviar.setOnClickListener { 
            val text = etMensaje.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                etMensaje.setText("")
            }
        }
        btnAdjuntar.setOnClickListener { 
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            selectImageLauncher.launch(intent)
        }
    }

    private fun getUniqueUserId(): String {
        val prefs = getSharedPreferences("ChatPrefs", MODE_PRIVATE)
        var id = prefs.getString("user_id", null)
        if (id == null) {
            id = "Invitado_" + UUID.randomUUID().toString().substring(0, 8)
            prefs.edit().putString("user_id", id).apply()
        }
        return id
    }

    private fun listenForMessages() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (postSnapshot in snapshot.children) {
                    val message = postSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                        // Si soy admin y el mensaje es de un invitado y no está leído, marcar como leído
                        if (esAdmin && message.senderId != "admin" && !message.read) {
                            postSnapshot.ref.child("read").setValue(true)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                rvChat.scrollToPosition(messages.size - 1)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendMessage(text: String) {
        val message = Message(senderId = currentUserId, text = text, timestamp = System.currentTimeMillis())
        dbRef.push().setValue(message)
    }

    private fun uploadImage(uri: Uri) {
        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val layout = findViewById<RelativeLayout>(R.id.rootLayoutChat) ?: findViewById(android.R.id.content)
        val params = RelativeLayout.LayoutParams(100, 100).apply {
            addRule(RelativeLayout.CENTER_IN_PARENT)
        }
        layout.addView(progressBar, params)

        val storageRef = FirebaseStorage.getInstance().getReference("chat_images/${UUID.randomUUID()}")
        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val message = Message(senderId = currentUserId, imageUrl = downloadUri.toString(), timestamp = System.currentTimeMillis())
                    dbRef.push().setValue(message)
                    layout.removeView(progressBar)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir imagen: ${it.message}", Toast.LENGTH_SHORT).show()
                layout.removeView(progressBar)
            }
    }
}
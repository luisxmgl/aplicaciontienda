package com.example.aplicaciontienda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AdminChatListActivity : AppCompatActivity() {

    private lateinit var rvChats: RecyclerView
    private val chatList = mutableListOf<ChatSummary>()
    private lateinit var adapter: AdminChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_chat_list)

        PresenceManager.updateLastSeen("admin")

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAdmin)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.title = "Mensajes Recibidos"
            toolbar.setNavigationOnClickListener {
                finish()
            }
            
            // Botón opcional para cerrar sesión si se desea
            toolbar.setOnLongClickListener {
                getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE).edit().clear().apply()
                Toast.makeText(this, "Sesión cerrada. Reinicie para loguear de nuevo.", Toast.LENGTH_LONG).show()
                true
            }
        }

        rvChats = findViewById(R.id.rvChats)
        rvChats.layoutManager = LinearLayoutManager(this)
        adapter = AdminChatAdapter(chatList) { chatId ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("ES_ADMIN", true)
            intent.putExtra("TARGET_ID", chatId)
            startActivity(intent)
        }
        rvChats.adapter = adapter

        try {
            val dbRef = FirebaseDatabase.getInstance().getReference("chats")
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatList.clear()
                    for (postSnapshot in snapshot.children) {
                        val chatId = postSnapshot.key ?: continue
                        var lastMsgText = "Imagen"
                        var lastTimestamp = 0L
                        var hasUnread = false
                        
                        for (msgSnap in postSnapshot.children) {
                            val msg = msgSnap.getValue(Message::class.java) ?: continue
                            if (msg.timestamp > lastTimestamp) {
                                lastTimestamp = msg.timestamp
                                lastMsgText = msg.text.ifEmpty { "Imagen" }
                            }
                            if (msg.senderId != "admin" && !msg.read) {
                                hasUnread = true
                            }
                        }
                        
                        chatList.add(ChatSummary(chatId, lastMsgText, hasUnread, lastTimestamp))
                    }
                    fetchPresenceAndSort()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminChatListActivity, "Error Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Firebase no configurado. Falta google-services.json", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchPresenceAndSort() {
        val presRef = FirebaseDatabase.getInstance().getReference("presence")
        presRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedList = chatList.map { summary ->
                    val lastSeen = snapshot.child(summary.chatId).getValue(Long::class.java) ?: 0L
                    summary.copy(lastSeen = lastSeen)
                }
                chatList.clear()
                chatList.addAll(updatedList)
                chatList.sortByDescending { it.timestamp }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

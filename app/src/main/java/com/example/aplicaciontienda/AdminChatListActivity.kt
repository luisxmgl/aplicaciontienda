package com.example.aplicaciontienda

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class AdminChatListActivity : AppCompatActivity() {

    private lateinit var lvChats: ListView
    private val chatList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_chat_list)

        supportActionBar?.title = "Mensajes Recibidos"

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAdmin).setNavigationOnClickListener {
            finish()
        }

        lvChats = findViewById(R.id.lvChats)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, chatList)
        lvChats.adapter = adapter

        try {
            val dbRef = FirebaseDatabase.getInstance().getReference("chats")
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatList.clear()
                    for (postSnapshot in snapshot.children) {
                        postSnapshot.key?.let { chatList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Firebase no configurado. Falta google-services.json", Toast.LENGTH_LONG).show()
        }

        lvChats.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("ES_ADMIN", true)
            intent.putExtra("TARGET_ID", chatList[position])
            startActivity(intent)
        }
    }
}
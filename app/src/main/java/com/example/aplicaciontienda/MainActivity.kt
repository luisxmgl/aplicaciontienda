package com.example.aplicaciontienda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import java.util.UUID

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContentView(R.layout.activity_main)

        val btnInvitado = findViewById<Button>(R.id.btnInvitado)

        btnInvitado.setOnClickListener {
            val intent = Intent(this, MainSelectorActivity::class.java)
            intent.putExtra("ES_ADMIN", false)
            startActivity(intent)
        }

        val guestId = getUniqueUserId()
        PresenceManager.updateLastSeen(guestId)

        setupBanner()

        findViewById<android.widget.Button>(R.id.btnAdminLoginReal).setOnClickListener {
            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)
        }

        findViewById<android.widget.TextView>(R.id.tvTitle2).setOnLongClickListener {
            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)
            true
        }
    }

    private fun setupBanner() {
        // ... implementation ...
    }

    private fun getUniqueUserId(): String {
        val prefs = getSharedPreferences("ChatPrefs", Context.MODE_PRIVATE)
        var id = prefs.getString("user_id", null)
        if (id == null) {
            id = "Invitado_" + UUID.randomUUID().toString().substring(0, 8)
            prefs.edit().putString("user_id", id).apply()
        }
        return id
    }
}

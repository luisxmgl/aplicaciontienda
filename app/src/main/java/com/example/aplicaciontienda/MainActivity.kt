package com.example.aplicaciontienda

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        val btnInvitado = findViewById<Button>(R.id.btnInvitado)

        btnInvitado.setOnClickListener {
            val intent = Intent(this, MainSelectorActivity::class.java)
            intent.putExtra("ES_ADMIN", false)
            startActivity(intent)
        }

        findViewById<android.widget.TextView>(R.id.btnAdminLogin).setOnClickListener {
            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)
        }

        findViewById<android.widget.TextView>(R.id.tvTitle).setOnLongClickListener {
            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)
            true
        }
    }
}

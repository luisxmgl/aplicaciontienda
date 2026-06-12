package com.example.aplicaciontienda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AdminLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Mover setContentView al principio para asegurar que el contexto de la activity esté listo
        setContentView(R.layout.activity_admin_login)
        
        val prefs = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("remembered", false)) {
            irAMainSelectorComoAdmin()
            return
        }

        val etUser = findViewById<TextInputEditText>(R.id.etUser)
        val etPass = findViewById<TextInputEditText>(R.id.etPass)
        val cbRemember = findViewById<CheckBox>(R.id.cbRemember)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val btnBack = findViewById<TextView>(R.id.btnBackToApp)

        btnLogin.setOnClickListener {
            val user = etUser.text.toString()
            val pass = etPass.text.toString()

            if (user == "administrador" && pass == "2026") {
                if (cbRemember.isChecked) {
                    prefs.edit().putBoolean("remembered", true).apply()
                }
                irAMainSelectorComoAdmin()
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun irAMainSelectorComoAdmin() {
        val intent = Intent(this, MainSelectorActivity::class.java)
        intent.putExtra("ES_ADMIN", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

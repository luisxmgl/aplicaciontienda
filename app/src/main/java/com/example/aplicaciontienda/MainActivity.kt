package com.example.aplicaciontienda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var layoutBotonesInicio: View
    private lateinit var layoutLoginAdmin: View
    private lateinit var etUsuario: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var cbRecordar: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        layoutBotonesInicio = findViewById(R.id.layoutBotonesInicio)
        layoutLoginAdmin = findViewById(R.id.layoutLoginAdmin)
        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        cbRecordar = findViewById(R.id.cbRecordar)

        val btnInvitado = findViewById<Button>(R.id.btnInvitado)
        val btnAdmin = findViewById<Button>(R.id.btnAdmin)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        val prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("recordar", false)) {
            etUsuario.setText(prefs.getString("usuario", ""))
            etPassword.setText(prefs.getString("password", ""))
            cbRecordar.isChecked = true
        }

        btnInvitado.setOnClickListener {
            startActivity(Intent(this, MainSelectorActivity::class.java))
        }

        btnAdmin.setOnClickListener {
            // Si ya es admin y guardó sesión, ir directo al selector principal como admin
            if (esAdminAutenticado()) {
                irATienda(esAdmin = true)
            } else {
                mostrarLoginAdmin(true)
            }
        }

        btnVolver.setOnClickListener {
            mostrarLoginAdmin(false)
        }

        btnIniciarSesion.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (usuario == "luismgl" && pass == "2050") {
                val editor = prefs.edit()
                if (cbRecordar.isChecked) {
                    editor.putString("usuario", usuario)
                    editor.putString("password", pass)
                    editor.putBoolean("recordar", true)
                } else {
                    editor.putBoolean("recordar", false)
                    editor.remove("usuario")
                    editor.remove("password")
                }
                editor.apply()
                irATienda(esAdmin = true)
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarLoginAdmin(mostrar: Boolean) {
        if (mostrar) {
            layoutBotonesInicio.visibility = View.GONE
            layoutLoginAdmin.visibility = View.VISIBLE
        } else {
            layoutBotonesInicio.visibility = View.VISIBLE
            layoutLoginAdmin.visibility = View.GONE
        }
    }

    private fun esAdminAutenticado(): Boolean {
        val prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        return prefs.getString("usuario", "") == "luismgl" && prefs.getBoolean("recordar", false)
    }

    private fun irATienda(esAdmin: Boolean) {
        val intent = Intent(this, MainSelectorActivity::class.java)
        intent.putExtra("ES_ADMIN", esAdmin)
        startActivity(intent)
        if (esAdmin) finish() // Evitar volver al login si ya entró como admin
    }
}
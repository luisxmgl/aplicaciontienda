package com.example.aplicaciontienda

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnInvitado = findViewById<Button>(R.id.btnInvitado)
        val btnAdmin = findViewById<Button>(R.id.btnAdmin)

        btnInvitado.setOnClickListener {
            irATienda(esAdmin = false)
        }

        btnAdmin.setOnClickListener {
            mostrarLoginAdmin()
        }
    }

    private fun mostrarLoginAdmin() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 0)

        val inputUsuario = EditText(this)
        inputUsuario.hint = "Usuario"
        layout.addView(inputUsuario)

        val inputPass = EditText(this)
        inputPass.hint = "Contraseña"
        inputPass.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(inputPass)

        AlertDialog.Builder(this)
            .setTitle("Acceso Administrador")
            .setView(layout)
            .setPositiveButton("Entrar") { _, _ ->
                val usuario = inputUsuario.text.toString()
                val pass = inputPass.text.toString()

                if (usuario == "luismgl" && pass == "2050") {
                    irATienda(esAdmin = true)
                } else {
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun irATienda(esAdmin: Boolean) {
        val intent = Intent(this, TiendaActivity::class.java)
        intent.putExtra("ES_ADMIN", esAdmin)
        startActivity(intent)
    }
}
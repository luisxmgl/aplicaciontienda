package com.example.aplicaciontienda

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class TrackingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarTracking)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val etCode = findViewById<TextInputEditText>(R.id.etTrackingCode)
        val btnSearch = findViewById<MaterialButton>(R.id.btnSearchTracking)
        val cardStatus = findViewById<MaterialCardView>(R.id.cardStatus)
        val tvOrderTitle = findViewById<TextView>(R.id.tvOrderTitle)
        val tvNote = findViewById<TextView>(R.id.tvStatusNote)

        val imgStep1 = findViewById<ImageView>(R.id.imgStep1)
        val imgStep2 = findViewById<ImageView>(R.id.imgStep2)
        val imgStep3 = findViewById<ImageView>(R.id.imgStep3)

        btnSearch.setOnClickListener {
            val code = etCode.text.toString()
            if (code.length == 4) {
                cardStatus.visibility = View.VISIBLE
                tvOrderTitle.text = "Pedido #$code"
                
                // Simulación de estados para demostración
                val lastDigit = code.last().toString().toInt()
                when {
                    lastDigit < 3 -> {
                        // Estado 1: Recibido
                        imgStep1.setImageResource(android.R.drawable.checkbox_on_background)
                        imgStep2.setImageResource(android.R.drawable.radiobutton_off_background)
                        imgStep3.setImageResource(android.R.drawable.radiobutton_off_background)
                        tvNote.text = "Hemos recibido tu pago. Tu pedido está en espera de entrar al taller."
                    }
                    lastDigit < 7 -> {
                        // Estado 2: Confección
                        imgStep1.setImageResource(android.R.drawable.checkbox_on_background)
                        imgStep2.setImageResource(android.R.drawable.checkbox_on_background)
                        imgStep3.setImageResource(android.R.drawable.radiobutton_off_background)
                        tvNote.text = "¡Buenas noticias! Tu pedido está siendo confeccionado o bordado en este momento."
                    }
                    else -> {
                        // Estado 3: Listo
                        imgStep1.setImageResource(android.R.drawable.checkbox_on_background)
                        imgStep2.setImageResource(android.R.drawable.checkbox_on_background)
                        imgStep3.setImageResource(android.R.drawable.checkbox_on_background)
                        tvNote.text = "¡Tu pedido está LISTO! Puedes pasar a la tienda a retirarlo con la cajera."
                    }
                }
            } else {
                Toast.makeText(this, "Ingresa un código válido de 4 dígitos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
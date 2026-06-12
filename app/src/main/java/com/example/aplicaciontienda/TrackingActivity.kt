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

        val intentCode = intent.getStringExtra("ORDER_CODE")
        if (intentCode != null) {
            etCode.setText(intentCode)
            // Automáticamente buscar si se pasó un código
            buscarPedido(intentCode, cardStatus, tvOrderTitle, imgStep1, imgStep2, imgStep3, tvNote)
        }

        btnSearch.setOnClickListener {
            val code = etCode.text.toString()
            if (code.length == 4) {
                buscarPedido(code, cardStatus, tvOrderTitle, imgStep1, imgStep2, imgStep3, tvNote)
            } else {
                Toast.makeText(this, "Ingresa un código válido de 4 dígitos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarPedido(
        code: String,
        cardStatus: View,
        tvOrderTitle: TextView,
        imgStep1: ImageView,
        imgStep2: ImageView,
        imgStep3: ImageView,
        tvNote: TextView
    ) {
        val database = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("pedidos")
        database.child(code).addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val pedido = snapshot.getValue(Pedido::class.java)
                if (pedido != null) {
                    cardStatus.visibility = View.VISIBLE
                    tvOrderTitle.text = "Pedido #$code"
                    
                    when (pedido.estado) {
                        1 -> {
                            imgStep1.setImageResource(android.R.drawable.checkbox_on_background)
                            imgStep2.setImageResource(android.R.drawable.radiobutton_off_background)
                            imgStep3.setImageResource(android.R.drawable.radiobutton_off_background)
                            tvNote.text = "Hemos recibido tu pago. Tu pedido está en espera de entrar al taller."
                        }
                        2 -> {
                            imgStep1.setImageResource(android.R.drawable.checkbox_on_background)
                            imgStep2.setImageResource(android.R.drawable.checkbox_on_background)
                            imgStep3.setImageResource(android.R.drawable.radiobutton_off_background)
                            tvNote.text = "¡Buenas noticias! Tu pedido está siendo confeccionado o bordado en este momento."
                        }
                        3 -> {
                            imgStep1.setImageResource(android.R.drawable.checkbox_on_background)
                            imgStep2.setImageResource(android.R.drawable.checkbox_on_background)
                            imgStep3.setImageResource(android.R.drawable.checkbox_on_background)
                            tvNote.text = "¡Tu pedido está LISTO! Puedes pasar a la tienda a retirarlo con la cajera."
                        }
                    }
                } else {
                    cardStatus.visibility = View.GONE
                    Toast.makeText(this@TrackingActivity, "No se encontró un pedido con ese código", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(this@TrackingActivity, "Error al conocer estado", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
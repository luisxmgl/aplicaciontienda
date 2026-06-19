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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TrackingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_tracking)
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
            return
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarTracking)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener { finish() }
        }

        val etCode = findViewById<TextInputEditText>(R.id.etTrackingCode)
        val btnSearch = findViewById<MaterialButton>(R.id.btnSearchTracking)
        
        val cardStatus = findViewById<MaterialCardView>(R.id.cardStatus)
        cardStatus?.visibility = View.GONE

        // Recuperar código de Mis Pedidos o de la pantalla anterior
        val intentCode = intent?.getStringExtra("ORDER_CODE")
        if (!intentCode.isNullOrEmpty()) {
            etCode?.setText(intentCode)
            // Pequeño delay para asegurar que la UI esté lista
            etCode?.post {
                buscarPedido(intentCode)
            }
        }

        btnSearch?.setOnClickListener {
            val code = etCode?.text.toString().trim()
            if (code.isNotEmpty()) {
                buscarPedido(code)
            } else {
                Toast.makeText(this, "Ingresa un código de retiro", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarPedido(code: String) {
        val cardStatus = findViewById<MaterialCardView>(R.id.cardStatus)
        val tvOrderTitle = findViewById<TextView>(R.id.tvOrderTitle)
        val tvNote = findViewById<TextView>(R.id.tvStatusNote)
        val imgStep1 = findViewById<ImageView>(R.id.imgStep1)
        val imgStep2 = findViewById<ImageView>(R.id.imgStep2)
        val imgStep3 = findViewById<ImageView>(R.id.imgStep3)

        cardStatus?.visibility = View.GONE

        try {
            val database = FirebaseDatabase.getInstance().getReference("pedidos")
            database.child(code).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isFinishing) return

                    if (!snapshot.exists()) {
                        Toast.makeText(this@TrackingActivity, "No se encontró el pedido #$code", Toast.LENGTH_SHORT).show()
                        return
                    }

                    try {
                        val pedido = snapshot.getValue(Pedido::class.java)
                        if (pedido != null) {
                            cardStatus?.visibility = View.VISIBLE
                            tvOrderTitle?.text = "Pedido #$code"
                            
                            // Resetear iconos a estado apagado
                            imgStep1?.setImageResource(android.R.drawable.radiobutton_off_background)
                            imgStep2?.setImageResource(android.R.drawable.radiobutton_off_background)
                            imgStep3?.setImageResource(android.R.drawable.radiobutton_off_background)

                            when (pedido.estado) {
                                1 -> {
                                    imgStep1?.setImageResource(android.R.drawable.checkbox_on_background)
                                    tvNote?.text = "Hemos recibido tu pago. Tu pedido está en espera de entrar al taller."
                                }
                                2 -> {
                                    imgStep1?.setImageResource(android.R.drawable.checkbox_on_background)
                                    imgStep2?.setImageResource(android.R.drawable.checkbox_on_background)
                                    tvNote?.text = "¡Buenas noticias! Tu pedido está siendo confeccionado o bordado en este momento."
                                }
                                3 -> {
                                    imgStep1?.setImageResource(android.R.drawable.checkbox_on_background)
                                    imgStep2?.setImageResource(android.R.drawable.checkbox_on_background)
                                    imgStep3?.setImageResource(android.R.drawable.checkbox_on_background)
                                    tvNote?.text = "¡Tu pedido está LISTO! Puedes pasar a la tienda a retirarlo con la cajera."
                                }
                                else -> {
                                    tvNote?.text = "Consulta en tienda por el estado de tu pedido."
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@TrackingActivity, "Error al procesar los datos del pedido", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (isFinishing) return
                    Toast.makeText(this@TrackingActivity, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Firebase no configurado correctamente", Toast.LENGTH_LONG).show()
        }
    }
}

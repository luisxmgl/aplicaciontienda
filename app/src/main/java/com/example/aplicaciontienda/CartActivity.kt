package com.example.aplicaciontienda

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class CartActivity : AppCompatActivity() {

    private lateinit var tvTotal: TextView
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarCart)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvCart = findViewById<RecyclerView>(R.id.rvCart)
        rvCart.layoutManager = LinearLayoutManager(this)
        
        tvTotal = findViewById(R.id.tvCartTotal)
        
        adapter = CartAdapter(CartManager.getItems()) {
            actualizarTotal()
        }
        rvCart.adapter = adapter
        
        actualizarTotal()

        findViewById<android.view.View>(R.id.btnClearCart).setOnClickListener {
            if (CartManager.getItems().isNotEmpty()) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Vaciar carrito")
                    .setMessage("¿Estás seguro de que quieres eliminar todos los productos del carrito?")
                    .setPositiveButton("Sí") { _, _ ->
                        CartManager.clear()
                        adapter.notifyDataSetChanged()
                        actualizarTotal()
                        Toast.makeText(this, "Carrito vaciado", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        findViewById<MaterialButton>(R.id.btnCheckout).setOnClickListener {
            val items = CartManager.getItems()
            if (items.isNotEmpty()) {
                val sb = StringBuilder("¡Hola! Me gustaría realizar el siguiente pedido:\n\n")
                items.forEach { item ->
                    sb.append("• ${item.producto.nombre}\n")
                    sb.append("  Cantidad: ${item.cantidad}\n")
                    if (item.producto.colegio.isNotEmpty()) {
                        sb.append("  Colegio: ${item.producto.colegio}\n")
                    }
                    sb.append("\n")
                }
                sb.append("Total estimado: ${Utils.formatPrice(CartManager.getTotal())}")
                
                // Enviar pedido directamente por WhatsApp
                Utils.openWhatsApp(this, "+56920680021", sb.toString())
                
                // Limpiar carrito después de enviar
                CartManager.clear()
                finish()
            } else {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarTotal() {
        tvTotal.text = Utils.formatPrice(CartManager.getTotal())
        val emptyView = findViewById<android.view.View>(R.id.tvEmptyCart)
        val rvCart = findViewById<android.view.View>(R.id.rvCart)
        if (CartManager.getItems().isEmpty()) {
            emptyView.visibility = android.view.View.VISIBLE
            rvCart.visibility = android.view.View.GONE
        } else {
            emptyView.visibility = android.view.View.GONE
            rvCart.visibility = android.view.View.VISIBLE
        }
    }
}
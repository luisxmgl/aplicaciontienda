package com.example.aplicaciontienda

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ProductDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val producto = intent.getSerializableExtra("PRODUCTO") as? Producto
        if (producto == null) {
            finish()
            return
        }

        findViewById<TextView>(R.id.tvDetailName).text = producto.nombre
        findViewById<TextView>(R.id.tvDetailPrice).text = Utils.formatPrice(producto.precio)
        val tvStock = findViewById<TextView>(R.id.tvDetailStock)
        tvStock.text = "Stock disponible: ${producto.stock}"
        tvStock.visibility = View.VISIBLE
        
        // Quitar placeholder de imagen
        findViewById<View>(R.id.ivProductLarge).visibility = View.GONE
        (findViewById<View>(R.id.ivProductLarge).parent as? View)?.visibility = View.GONE

        val cgSizes = findViewById<ChipGroup>(R.id.cgSizes)
        // La talla ya viene en el producto en el nuevo catálogo
        val chip = Chip(this)
        chip.text = producto.talla
        chip.isCheckable = true
        chip.isChecked = true
        cgSizes.addView(chip)

        findViewById<MaterialButton>(R.id.btnAddToCart).setOnClickListener {
            CartManager.addItem(producto)
            Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<MaterialButton>(R.id.btnWhatsAppDetail).setOnClickListener {
            val message = "Hola! Me interesa el producto: ${producto.nombre}, talla: ${producto.talla} del colegio: ${producto.colegio}"
            Utils.openWhatsApp(this, "56920680021", message)
        }

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }
}

package com.example.aplicaciontienda

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

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
        findViewById<TextView>(R.id.tvDescription).text = producto.descripcion
        findViewById<TextView>(R.id.tvDetailStock).visibility = View.GONE
        
        // Quitar placeholder de imagen
        findViewById<View>(R.id.ivProductLarge).visibility = View.GONE
        (findViewById<View>(R.id.ivProductLarge).parent as? View)?.visibility = View.GONE

        // La talla ya viene en el nombre del producto, ocultamos el selector
        findViewById<View>(R.id.cgSizes).visibility = View.GONE
        findViewById<View>(R.id.tvLabelTalla).visibility = View.GONE

        findViewById<MaterialButton>(R.id.btnAddToCart).setOnClickListener {
            CartManager.addItem(producto)
            Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<MaterialButton>(R.id.btnWhatsAppDetail).setOnClickListener {
            val message = "Hola! Me interesa el producto: ${producto.nombre} del colegio: ${producto.colegio}"
            Utils.openWhatsApp(this, "56920680021", message)
        }

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }
}

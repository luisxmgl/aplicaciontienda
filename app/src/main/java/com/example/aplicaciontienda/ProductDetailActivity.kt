package com.example.aplicaciontienda

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var repository: CatalogRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        repository = CatalogRepository(this)
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

        val btnRedeem = findViewById<MaterialButton>(R.id.btnRedeemPoints)
        val puntosNecesarios = producto.puntosCost
        btnRedeem.text = "Canjear por $puntosNecesarios Villa Puntos"
        
        btnRedeem.setOnClickListener {
            if (PointsManager.getPoints(this) >= puntosNecesarios) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Canjear Producto")
                    .setMessage("¿Deseas canjear ${producto.nombre} por $puntosNecesarios Villa Puntos?")
                    .setPositiveButton("Canjear") { _, _ ->
                        if (PointsManager.redeemPoints(this, puntosNecesarios)) {
                            // Generamos un pedido especial de canje
                            val orderCode = Utils.generateOrderCode()
                            saveRedeemToFirebase(orderCode, producto)
                            Toast.makeText(this, "¡Canje exitoso! Tu código: $orderCode", Toast.LENGTH_LONG).show()
                            LocalOrdersManager.saveOrder(this, orderCode)
                            finish()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                Toast.makeText(this, "No tienes suficientes Villa Puntos (Tienes: ${PointsManager.getPoints(this)})", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnWhatsAppDetail).setOnClickListener {
            val message = "Hola! Me interesa el producto: ${producto.nombre} del colegio: ${producto.colegio}"
            Utils.openWhatsApp(this, "56920680021", message)
        }

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }

        setupRelatedProducts(producto)
    }

    private fun saveRedeemToFirebase(code: String, producto: Producto) {
        val database = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("pedidos")
        val orderId = database.push().key ?: return

        val itemPedido = listOf(
            CartItemPedido(
                nombre = producto.nombre,
                talla = producto.talla,
                precio = 0,
                cantidad = 1,
                colegio = producto.colegio
            )
        )

        val pedido = Pedido(
            id = orderId,
            codigoRetiro = code,
            items = itemPedido,
            total = 0,
            extraCharge = 0,
            customization = "CANJE POR VILLA PUNTOS",
            estado = 1
        )

        database.child(code).setValue(pedido)
    }

    private fun setupRelatedProducts(currentProduct: Producto) {
        val rvRelated = findViewById<RecyclerView>(R.id.rvRelatedProducts)
        rvRelated.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        lifecycleScope.launch {
            try {
                val data = repository.getCatalogData()
                val related = data.find { it.nombre == currentProduct.colegio }?.productos
                    ?.filter { it.nombre != currentProduct.nombre }
                    ?.take(6) ?: emptyList()

                if (related.isNotEmpty()) {
                    rvRelated.adapter = ProductoAdapter(related)
                } else {
                    findViewById<View>(R.id.rvRelatedProducts).visibility = View.GONE
                    // También podríamos ocultar el título de "Completa el uniforme"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

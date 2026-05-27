package com.example.aplicaciontienda

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TiendaActivity : AppCompatActivity() {

    private lateinit var adapter: ProductoAdapter
    private val productos = mutableListOf<Producto>()
    private var esAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tienda)

        esAdmin = intent.getBooleanExtra("ES_ADMIN", false)
        supportActionBar?.title = if (esAdmin) "Tienda (Admin)" else "Tienda (Invitado)"

        cargarProductosDesdeAssets()

        val rvProductos = findViewById<RecyclerView>(R.id.rvProductos)
        rvProductos.layoutManager = LinearLayoutManager(this)
        adapter = ProductoAdapter(productos) {
            // Actualización de UI si es necesario
        }
        rvProductos.adapter = adapter

        findViewById<Button>(R.id.btnWhatsApp).setOnClickListener {
            enviarPedidoWhatsApp()
        }

        findViewById<Button>(R.id.btnSobreNosotros).setOnClickListener {
            mostrarSobreNosotros()
        }

        findViewById<Button>(R.id.btnInstagram).setOnClickListener {
            abrirInstagram()
        }
    }

    private fun cargarProductosDesdeAssets() {
        try {
            val jsonString = assets.open("catalogo.json").bufferedReader().use { it.readText() }
            val colegiosArray = JSONArray(jsonString)

            for (i in 0 until colegiosArray.length()) {
                val colegioObj = colegiosArray.getJSONObject(i)
                val nombreColegio = colegioObj.getString("nombre")
                val productosArray = colegioObj.getJSONArray("productos")

                for (j in 0 until productosArray.length()) {
                    val prodObj = productosArray.getJSONObject(j)
                    productos.add(Producto(
                        nombre = prodObj.getString("tipoPrenda"),
                        talla = prodObj.getString("talla"),
                        precio = prodObj.getInt("precio"),
                        colegio = nombreColegio
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar el catálogo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarPedidoWhatsApp() {
        val seleccionados = productos.filter { it.cantidad > 0 }
        if (seleccionados.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        var mensaje = "Hola, he seleccionado los siguientes productos:\n\n"
        var total = 0
        seleccionados.forEach {
            mensaje += "- ${it.cantidad}x ${it.nombre} Talla ${it.talla} (${it.colegio}): $${it.precio * it.cantidad}\n"
            total += it.precio * it.cantidad
        }
        mensaje += "\nTotal: $$total"

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val url = "https://api.whatsapp.com/send?phone=56920680021&text=" + URLEncoder.encode(mensaje, "UTF-8")
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarSobreNosotros() {
        AlertDialog.Builder(this)
            .setTitle("Sobre Nosotros")
            .setMessage("Tienda de Uniformes - Confecciones Villa Acero\n\n¡Calidad en cada prenda!")
            .setPositiveButton("Ver Ubicación") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/N9L73KwWQ2Xotv9P8"))
                startActivity(intent)
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun abrirInstagram() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/confecciones.villaacero/?hl=es"))
        try {
            intent.setPackage("com.instagram.android")
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/confecciones.villaacero/?hl=es")))
        }
    }
}
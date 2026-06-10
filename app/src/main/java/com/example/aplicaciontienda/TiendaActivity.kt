package com.example.aplicaciontienda

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class TiendaActivity : AppCompatActivity() {

    private lateinit var adapter: ProductoAdapter
    private val todosLosProductos = mutableListOf<Producto>()
    private val productosFiltrados = mutableListOf<Producto>()
    private var esAdmin: Boolean = false
    private lateinit var repository: CatalogRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tienda)

        repository = CatalogRepository(this)
        esAdmin = intent.getBooleanExtra("ES_ADMIN", false)
        val codFamilia = intent.getStringExtra("COD_FAMILIA") // Usamos codfamilia ahora
        val colegioNombre = intent.getStringExtra("COLEGIO_NOMBRE") ?: "Tienda"

        findViewById<TextView>(R.id.tvColegioNombre).text = colegioNombre
        
        val rvProductos = findViewById<RecyclerView>(R.id.rvProductos)
        rvProductos.layoutManager = GridLayoutManager(this, 2) 
        
        adapter = ProductoAdapter(productosFiltrados)
        rvProductos.adapter = adapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnCart).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnChat).setOnClickListener {
            if (esAdmin) {
                startActivity(Intent(this, AdminChatListActivity::class.java))
            } else {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("COLEGIO_NOMBRE", colegioNombre)
                intent.putExtra("ES_ADMIN", esAdmin)
                startActivity(intent)
            }
        }

        findViewById<ImageButton>(R.id.btnInfo).setOnClickListener {
            startActivity(Intent(this, SobreNosotrosActivity::class.java))
        }

        cargarProductos(codFamilia)
    }

    private fun cargarProductos(codFamilia: String?) {
        lifecycleScope.launch {
            try {
                val data = repository.getCatalogData()
                todosLosProductos.clear()
                
                // Filtrar por familia si se proporcionó una
                val productos = if (codFamilia != null) {
                    data.productos.filter { it.codfamilia == codFamilia }
                } else {
                    data.productos
                }
                
                todosLosProductos.addAll(productos)
                productosFiltrados.clear()
                productosFiltrados.addAll(todosLosProductos)
                
                configurarFiltros()
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun configurarFiltros() {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
        chipGroup.removeAllViews()
        
        val categorias = todosLosProductos.map { it.nombre.substringBefore(" T-").trim() }.distinct().sorted()
        
        val chipTodos = com.google.android.material.chip.Chip(this)
        chipTodos.text = "TODOS"
        chipTodos.isCheckable = true
        chipTodos.isChecked = true
        chipTodos.id = View.generateViewId()
        chipGroup.addView(chipTodos)

        categorias.forEach { cat ->
            val chip = com.google.android.material.chip.Chip(this)
            chip.text = cat
            chip.isCheckable = true
            chip.id = View.generateViewId()
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                productosFiltrados.clear()
                productosFiltrados.addAll(todosLosProductos)
            } else {
                val selectedId = checkedIds.first()
                val selectedChip = group.findViewById<com.google.android.material.chip.Chip>(selectedId)
                val category = selectedChip.text.toString()

                productosFiltrados.clear()
                if (category == "TODOS") {
                    productosFiltrados.addAll(todosLosProductos)
                } else {
                    productosFiltrados.addAll(todosLosProductos.filter { it.nombre.startsWith(category) })
                }
            }
            adapter.notifyDataSetChanged()
        }
    }
}

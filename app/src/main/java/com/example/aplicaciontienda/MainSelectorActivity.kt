package com.example.aplicaciontienda

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class MainSelectorActivity : AppCompatActivity() {

    private lateinit var adapter: ColegioAdapter
    private val todosLosColegios = mutableListOf<Colegio>()
    private var filteredList = mutableListOf<Colegio>()
    private var selectedComuna: String = "Todas"
    private var currentSearchQuery: String = ""
    private var esAdmin: Boolean = false
    private lateinit var repository: CatalogRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_selector)

        repository = CatalogRepository(this)
        esAdmin = false // Acceso solo como invitado
        
        val fabChat = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabChat)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        
        fabChat.visibility = View.VISIBLE
        fabChat.setOnClickListener {
            Utils.openWhatsApp(this, "+56920680021", "Hola! Quería consultar sobre los uniformes.")
        }
        btnLogout.visibility = View.GONE

        val rvColegios = findViewById<RecyclerView>(R.id.rvColegios)
        rvColegios.layoutManager = LinearLayoutManager(this)
        
        adapter = ColegioAdapter(filteredList) { colegio ->
            val intent = Intent(this, TiendaActivity::class.java)
            intent.putExtra("COD_FAMILIA", colegio.id.toString())
            intent.putExtra("COLEGIO_NOMBRE", colegio.nombre)
            intent.putExtra("ES_ADMIN", esAdmin)
            startActivity(intent)
        }
        rvColegios.adapter = adapter

        findViewById<ImageButton>(R.id.btnInfo).setOnClickListener {
            val intent = Intent(this, SobreNosotrosActivity::class.java)
            intent.putExtra("ES_ADMIN", esAdmin)
            startActivity(intent)
        }

        cargarColegios()
        configurarBusqueda()
        configurarFiltrosComuna()
    }

    private fun configurarFiltrosComuna() {
        val cgComunas = findViewById<ChipGroup>(R.id.cgComunas)
        cgComunas.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                selectedComuna = "Todas"
            } else {
                val selectedId = checkedIds.first()
                val chip = findViewById<Chip>(selectedId)
                selectedComuna = chip.text.toString()
            }
            aplicarFiltros()
        }
    }

    private fun configurarBusqueda() {
        val etSearch = findViewById<EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun aplicarFiltros() {
        val lowerCaseQuery = currentSearchQuery.lowercase()
        val result = todosLosColegios.filter { 
            (selectedComuna == "Todas" || it.comuna == selectedComuna) &&
            (it.nombre.lowercase().contains(lowerCaseQuery) || 
             it.comuna.lowercase().contains(lowerCaseQuery))
        }
        filteredList.clear()
        filteredList.addAll(result)
        adapter.notifyDataSetChanged()
    }

    private fun cargarColegios() {
        lifecycleScope.launch {
            try {
                val data = repository.getCatalogData()
                todosLosColegios.clear()


                data.forEach { ui ->
                    todosLosColegios.add(Colegio(
                        id = ui.id,
                        nombre = ui.nombre,
                        comuna = ui.comuna,
                        direccion = "",
                        logo = ui.logo,
                        productos = ui.productos
                    ))
                }
                
                aplicarFiltros()

                findViewById<TextView>(R.id.tvCountHeader).text = "Selecciona tu colegio"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

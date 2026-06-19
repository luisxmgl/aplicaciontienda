package com.example.aplicaciontienda

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
        esAdmin = intent.getBooleanExtra("ES_ADMIN", false)
        
        val fabChat = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabChat)
        
        fabChat.visibility = if (esAdmin) View.GONE else View.VISIBLE
        fabChat.setOnClickListener {
            Utils.openWhatsApp(this, "+56920680021", "Hola! Quería consultar sobre los uniformes.")
        }
        
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
        configurarNavegacion()
        escucharMensajesNuevos()

        findViewById<View>(R.id.btnBackStart)?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun escucharMensajesNuevos() {
        if (!esAdmin) return
        
        val dbRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("chats")
        dbRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                var totalChatsConPendientes = 0
                for (chatSnap in snapshot.children) {
                    var tienePendientes = false
                    for (msgSnap in chatSnap.children) {
                        val msg = msgSnap.getValue(Message::class.java)
                        if (msg != null && msg.senderId != "admin" && !msg.read) {
                            tienePendientes = true
                            break
                        }
                    }
                    if (tienePendientes) totalChatsConPendientes++
                }
                actualizarBadge(totalChatsConPendientes)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    private fun actualizarBadge(count: Int) {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        if (bottomNav == null) return
        
        val badge = bottomNav.getOrCreateBadge(R.id.nav_chat) 
        if (count > 0) {
            badge.isVisible = true
            badge.number = count
            badge.backgroundColor = android.graphics.Color.RED
            badge.badgeTextColor = android.graphics.Color.WHITE
        } else {
            badge.isVisible = false
        }
    }

    private fun configurarNavegacion() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        if (bottomNav == null) return

        // IMPORTANTE: Desactivar el tinte automático para que los iconos PNG se vean con sus colores originales
        bottomNav.itemIconTintList = null

        // Si es admin, cambiamos el título del menú de seguimiento para que sea claro
        if (esAdmin) {
            val menu = bottomNav.menu
            menu.findItem(R.id.nav_chat)?.title = "Mensajes"
            menu.findItem(R.id.nav_my_orders)?.title = "Pedidos"
            menu.findItem(R.id.nav_tracking)?.title = "Gestión"
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    if (esAdmin) {
                        startActivity(Intent(this, AdminChatListActivity::class.java))
                    } else {
                        startActivity(Intent(this, ChatActivity::class.java))
                    }
                    true
                }
                R.id.nav_my_orders -> {
                    if (!esAdmin) {
                        startActivity(Intent(this, MyOrdersActivity::class.java))
                    }
                    true
                }
                R.id.nav_tracking -> {
                    if (esAdmin) {
                        val intent = Intent(this, AdminOrdersActivity::class.java)
                        startActivity(intent)
                    } else {
                        startActivity(Intent(this, TrackingActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
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
                
                configurarChipsDinamicos()
                aplicarFiltros()

                findViewById<TextView>(R.id.tvCountHeader).text = "${todosLosColegios.size} colegios disponibles en la zona"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun configurarChipsDinamicos() {
        val cgComunas = findViewById<ChipGroup>(R.id.cgComunas)
        cgComunas.removeAllViews()

        val comunas = todosLosColegios.map { it.comuna }.distinct().sorted()
        
        val chipTodas = Chip(this)
        chipTodas.text = "Todas"
        chipTodas.isCheckable = true
        chipTodas.isChecked = true
        chipTodas.id = View.generateViewId()
        cgComunas.addView(chipTodas)

        comunas.forEach { comuna ->
            val chip = Chip(this)
            chip.text = comuna
            chip.isCheckable = true
            chip.id = View.generateViewId()
            cgComunas.addView(chip)
        }

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
}

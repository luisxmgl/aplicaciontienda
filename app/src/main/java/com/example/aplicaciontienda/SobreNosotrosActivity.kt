package com.example.aplicaciontienda

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton

class SobreNosotrosActivity : AppCompatActivity() {

    private var esAdmin: Boolean = false
    private lateinit var viewPager: ViewPager2
    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        viewPager.currentItem = viewPager.currentItem + 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sobre_nosotros)

        esAdmin = intent.getBooleanExtra("ES_ADMIN", false)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnWhatsApp).setOnClickListener {
            Utils.openWhatsApp(this, "+56920680021", "Hola! Quería consultar sobre los uniformes.")
        }

        findViewById<MaterialButton>(R.id.btnInstagram).setOnClickListener {
            Utils.openInstagram(this, "https://www.instagram.com/confecciones.villaacero/")
        }

        // Configurar Slideshow
        viewPager = findViewById(R.id.viewPagerSlideshow)
        val images = assets.list("slide")?.filter { it.endsWith(".jpg") || it.endsWith(".jpeg") } ?: emptyList()
        
        val adapterSlideshow = SlideshowAdapter(images)
        viewPager.adapter = adapterSlideshow

        if (images.isNotEmpty()) {
            val initialPos = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % images.size)
            viewPager.setCurrentItem(initialPos, false)
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 1400)
            }
        })

        val rvColegios = findViewById<RecyclerView>(R.id.rvColegios)
        rvColegios.layoutManager = LinearLayoutManager(this)
        
        val colegios = cargarColegios()
        rvColegios.adapter = ColegioAdapter(colegios) { colegio ->
            val intent = Intent(this, TiendaActivity::class.java)
            intent.putExtra("COLEGIO_ID", colegio.id)
            intent.putExtra("COLEGIO_NOMBRE", colegio.nombre)
            intent.putExtra("COLEGIO_COMUNA", colegio.comuna)
            intent.putExtra("ES_ADMIN", esAdmin)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 1400)
    }

    private fun cargarColegios(): List<Colegio> {
        val lista = mutableListOf<Colegio>()
        try {
            val jsonString = assets.open("catalogo_por_colegio.json").bufferedReader().use { it.readText() }
            val rootObj = org.json.JSONObject(jsonString)
            val array = rootObj.getJSONArray("colegios")
            
            val metadata = rootObj.getJSONObject("metadata")
            findViewById<TextView>(R.id.tvTotalProductosStat).text = metadata.getInt("total_productos").toString()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                
                lista.add(Colegio(
                    id = i, // Usamos el índice como ID
                    nombre = obj.getString("nombre"),
                    comuna = "Concepción",
                    direccion = "",
                    logo = Utils.getLogoForColegio(obj.getString("nombre")),
                    productos = emptyList()
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }
}
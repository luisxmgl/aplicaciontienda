package com.example.aplicaciontienda

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SobreNosotrosActivity : AppCompatActivity() {

    private var esAdmin: Boolean = false
    private lateinit var viewPager: ViewPager2
    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        viewPager.currentItem += 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sobre_nosotros)

        esAdmin = false // Solo invitado

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnWhatsApp).setOnClickListener {
            Utils.openWhatsApp(this, "+56920680021", "Hola! Quería consultar sobre los uniformes.")
        }

        findViewById<MaterialButton>(R.id.btnInstagram).setOnClickListener {
            Utils.openInstagram(this, "https://www.instagram.com/confecciones.villaacero/")
        }

        findViewById<MaterialButton>(R.id.btnMaps).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/maps/place/Confecciones+Villa+Acero/@-36.796784,-73.0902038,17z/data=!3m1!4b1!4m16!1m9!4m8!1m0!1m6!1m2!1s0x9669ca8024a50de1:0xb6df2801e6db18f1!2sConfecciones+Villa+Acero+-+Los+Poetas+8741,+4601963+Hualp%C3%A9n,+B%C3%ADo+B%C3%ADo!2m2!1d-73.0876289!2d-36.7967839!3m5!1s0x9669ca8024a50de1:0xb6df2801e6db18f1!8m2!3d-36.796784!4d-73.0876289!16s%2Fg%2F11clyd5j6g?entry=ttu&g_ep=EgoyMDI2MDYwMy4xIKXMDSoASAFQAw%3D%3D"))
            startActivity(intent)
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
        
        cargarDatos(rvColegios)
    }

    private fun cargarDatos(rvColegios: RecyclerView) {
        lifecycleScope.launch {
            try {
                val repository = CatalogRepository(this@SobreNosotrosActivity)
                val data = repository.getCatalogData()
                
                val colegios = data.map { ui ->
                    Colegio(
                        id = ui.id,
                        nombre = ui.nombre,
                        comuna = ui.comuna,
                        direccion = "",
                        logo = ui.logo,
                        productos = ui.productos
                    )
                }.sortedBy { it.nombre }

                rvColegios.adapter = ColegioAdapter(colegios, showCount = false) { colegio ->
                    val intent = Intent(this@SobreNosotrosActivity, TiendaActivity::class.java)
                    intent.putExtra("COD_FAMILIA", colegio.id)
                    intent.putExtra("COLEGIO_NOMBRE", colegio.nombre)
                    intent.putExtra("COLEGIO_COMUNA", colegio.comuna)
                    intent.putExtra("ES_ADMIN", esAdmin)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
}

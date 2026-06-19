package com.example.aplicaciontienda

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CatalogRepository(private val context: Context) {
    private val gson = Gson()

    data class WebColegio(
        val id: String,
        val nombre: String,
        val comuna: String,
        val productos: List<Producto>
    )

    suspend fun getCatalogData(): List<ColegioUI> = withContext(Dispatchers.IO) {
        val jsonString = context.assets.open("catalogo.json").bufferedReader().use { it.readText() }
        val listType = object : com.google.gson.reflect.TypeToken<List<WebColegio>>() {}.type
        val rawData: List<WebColegio> = gson.fromJson(jsonString, listType)

        return@withContext rawData.map { webColegio ->
            ColegioUI(
                id = webColegio.id,
                nombre = webColegio.nombre,
                comuna = webColegio.comuna,
                logo = Utils.getLogoForColegio(webColegio.nombre),
                productos = webColegio.productos.map { it.copy(nomfamilia = webColegio.nombre) }
            )
        }
    }
}

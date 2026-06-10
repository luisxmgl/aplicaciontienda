package com.example.aplicaciontienda

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CatalogRepository(private val context: Context) {
    private val gson = Gson()

    private val colegiosPermitidos = setOf(
        "SAGRADOS CORAZONES", "BAUTISTA", "INMACULADA", "ITAHUE", "PINARES",
        "PRESTON", "VILLA ACERO", "STA. LEONOR", "SAN CRISTOBAL", "HIGH SCOPE",
        "TJS", "CONCEPCION-PEDRO DE VALDIVIA", "KINGSTON COLLEGE"
    )

    suspend fun getCatalogData(): List<ColegioUI> = withContext(Dispatchers.IO) {
        val jsonString = context.assets.open("catalogo_limpio.json").bufferedReader().use { it.readText() }
        val rawData = gson.fromJson(jsonString, CatalogData::class.java)

        // Deduplicar productos por idproducto y filtrar por colegios permitidos
        val productosLimpios = rawData.productos
            .distinctBy { it.idproducto }
            .filter { it.nomfamilia.trim().uppercase() in colegiosPermitidos }

        // Agrupar productos por codfamilia
        val productosAgrupados = productosLimpios.groupBy { it.codfamilia }

        // Mapear a ColegioUI filtrando los permitidos y los que tienen productos
        return@withContext rawData.familias
            .filter { it.nomfamilia.trim().uppercase() in colegiosPermitidos }
            .mapNotNull { familia ->
                val productos = productosAgrupados[familia.codfamilia] ?: emptyList()
                if (productos.isNotEmpty()) {
                    ColegioUI(
                        id = familia.codfamilia,
                        nombre = familia.nomfamilia.trim(),
                        comuna = familia.comuna,
                        logo = Utils.getLogoForColegio(familia.nomfamilia),
                        productos = productos
                    )
                } else null
            }
    }
}

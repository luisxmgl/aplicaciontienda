package com.example.aplicaciontienda

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CatalogRepository(private val context: Context) {
    private val gson = Gson()
    
    // Lista de familias a excluir según requerimiento
    private val familiasExcluidas = setOf("5", "7", "21", "8", "17", "18")

    suspend fun getCatalogData(): CatalogData = withContext(Dispatchers.IO) {
        val jsonString = context.assets.open("catalogo_limpio.json").bufferedReader().use { it.readText() }
        val rawData = gson.fromJson(jsonString, CatalogData::class.java)
        
        // Filtrar familias y productos excluidos
        val filteredFamilias = rawData.familias.filter { it.codfamilia !in familiasExcluidas }
        val uniqueProducts = rawData.productos
            .filter { it.codfamilia !in familiasExcluidas }
            .distinctBy { it.idproducto }
        
        CatalogData(familias = filteredFamilias, productos = uniqueProducts)
    }
}

package cl.villaacero.uniformes.data

import android.content.Context
import cl.villaacero.uniformes.R
import kotlinx.serialization.json.Json

/**
 * Carga el catálogo desde res/raw/catalogo.json y expone los datos al resto de la app.
 * Es un singleton: se inicializa una vez en MainActivity.onCreate().
 */
object CatalogRepository {

    private var catalogo: Catalogo? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /** Carga el JSON. Llamar una sola vez al arranque. */
    fun init(context: Context) {
        if (catalogo != null) return
        val raw = context.resources
            .openRawResource(R.raw.catalogo)
            .bufferedReader()
            .use { it.readText() }
        catalogo = json.decodeFromString<Catalogo>(raw)
    }

    fun negocio(): Negocio = requireCatalogo().negocio

    fun colegios(): List<Colegio> = requireCatalogo().colegios

    fun colegioById(id: Int): Colegio? =
        requireCatalogo().colegios.find { it.id == id }

    /** Productos del colegio agrupados por tipo de prenda. */
    fun productosAgrupados(colegioId: Int): List<ProductoAgrupado> {
        val colegio = colegioById(colegioId) ?: return emptyList()
        return agruparProductosPorTipo(colegio.productos)
    }

    /** Tipos de prenda únicos en un colegio (para los filtros). */
    fun tiposDePrenda(colegioId: Int): List<String> {
        val colegio = colegioById(colegioId) ?: return emptyList()
        return tiposDePrendaUnicos(colegio.productos)
    }

    /** Comunas únicas (para el filtro de Home). */
    fun comunas(): List<String> = comunasUnicas(requireCatalogo().colegios)

    /** Total de productos en todo el catálogo. */
    fun totalProductos(): Int =
        requireCatalogo().colegios.sumOf { it.productos.size }

    private fun requireCatalogo(): Catalogo =
        catalogo ?: error("CatalogRepository.init() no fue llamado.")
}

package cl.villaacero.uniformes.ui.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cl.villaacero.uniformes.data.CatalogRepository
import cl.villaacero.uniformes.data.Colegio
import cl.villaacero.uniformes.data.Negocio
import cl.villaacero.uniformes.data.ProductoAgrupado
import cl.villaacero.uniformes.navigation.Screen
import cl.villaacero.uniformes.util.formatPrice

class DetalleViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val colegioId: Int = savedStateHandle.get<Int>(Screen.Detalle.ARG_COLEGIO_ID)
        ?: error("Falta colegioId en navegación")

    private val tipoPrenda: String = savedStateHandle.get<String>(Screen.Detalle.ARG_TIPO_PRENDA)
        ?: error("Falta tipoPrenda en navegación")

    val colegio: Colegio? = CatalogRepository.colegioById(colegioId)
    val negocio: Negocio = CatalogRepository.negocio()
    val producto: ProductoAgrupado? = CatalogRepository
        .productosAgrupados(colegioId)
        .find { it.tipoPrenda == tipoPrenda }

    fun mensajeWhatsappGenerico(): String {
        val colegioNombre = colegio?.nombre ?: "el colegio seleccionado"
        return "Hola! Me interesa el *$tipoPrenda* del colegio *$colegioNombre*. " +
                "¿Tienen disponibilidad?"
    }

    fun mensajeWhatsappConTalla(talla: String, precio: Int): String {
        val colegioNombre = colegio?.nombre ?: "el colegio seleccionado"
        val precioStr = formatPrice(precio)
        return "Hola! Me interesa el *$tipoPrenda* del colegio *$colegioNombre* " +
                "en talla *$talla* ($precioStr). ¿Tienen disponibilidad?"
    }
}

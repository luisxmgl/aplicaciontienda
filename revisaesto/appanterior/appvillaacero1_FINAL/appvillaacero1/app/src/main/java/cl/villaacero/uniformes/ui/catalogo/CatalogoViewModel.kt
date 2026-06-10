package cl.villaacero.uniformes.ui.catalogo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cl.villaacero.uniformes.data.CatalogRepository
import cl.villaacero.uniformes.data.Colegio
import cl.villaacero.uniformes.data.ProductoAgrupado
import cl.villaacero.uniformes.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CatalogoViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val colegioId: Int = savedStateHandle.get<Int>(Screen.Catalogo.ARG_COLEGIO_ID)
        ?: error("Falta colegioId en navegación")

    val colegio: Colegio? = CatalogRepository.colegioById(colegioId)
    val tiposPrenda: List<String> = CatalogRepository.tiposDePrenda(colegioId)

    private val _tipoFilter = MutableStateFlow<String?>(null)
    val tipoFilter: StateFlow<String?> = _tipoFilter.asStateFlow()

    private val todos: List<ProductoAgrupado> = CatalogRepository.productosAgrupados(colegioId)

    private val _productos = MutableStateFlow(todos)
    val productos: StateFlow<List<ProductoAgrupado>> = _productos.asStateFlow()

    fun setTipoFilter(tipo: String?) {
        _tipoFilter.value = tipo
        _productos.value = if (tipo == null) todos else todos.filter { it.tipoPrenda == tipo }
    }
}

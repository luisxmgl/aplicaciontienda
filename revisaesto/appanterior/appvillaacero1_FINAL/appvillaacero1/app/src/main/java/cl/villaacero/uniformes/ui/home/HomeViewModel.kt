package cl.villaacero.uniformes.ui.home

import androidx.lifecycle.ViewModel
import cl.villaacero.uniformes.data.CatalogRepository
import cl.villaacero.uniformes.data.Colegio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _comunaFilter = MutableStateFlow<String?>(null)
    val comunaFilter: StateFlow<String?> = _comunaFilter.asStateFlow()

    private val _colegios = MutableStateFlow(CatalogRepository.colegios().sortedBy { it.nombre })
    val colegios: StateFlow<List<Colegio>> = _colegios.asStateFlow()

    val comunas: List<String> = CatalogRepository.comunas()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        recompute()
    }

    fun setComunaFilter(comuna: String?) {
        _comunaFilter.value = comuna
        recompute()
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _comunaFilter.value = null
        recompute()
    }

    private fun recompute() {
        val query = _searchQuery.value.trim().lowercase()
        val comuna = _comunaFilter.value
        var list = CatalogRepository.colegios().sortedBy { it.nombre }
        if (comuna != null) {
            list = list.filter { it.comuna == comuna }
        }
        if (query.isNotEmpty()) {
            list = list.filter { it.nombre.lowercase().contains(query) }
        }
        _colegios.value = list
    }
}

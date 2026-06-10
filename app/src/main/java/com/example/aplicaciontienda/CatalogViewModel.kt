package com.example.aplicaciontienda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CatalogUiState {
    object Loading : CatalogUiState()
    data class Success(
        val familias: List<Familia>,
        val productosPorFamilia: Map<String, List<Producto>>
    ) : CatalogUiState()
    data class Error(val message: String) : CatalogUiState()
}

class CatalogViewModel(private val repository: CatalogRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    fun loadCatalog() {
        viewModelScope.launch {
            try {
                val data = repository.getCatalogData()
                // Agrupar productos por codfamilia
                val grouped = data.productos.groupBy { it.codfamilia }
                
                // Emitir un solo estado con toda la información cargada
                _uiState.emit(CatalogUiState.Success(data.familias, grouped))
            } catch (e: Exception) {
                _uiState.emit(CatalogUiState.Error(e.message ?: "Error desconocido"))
            }
        }
    }
}

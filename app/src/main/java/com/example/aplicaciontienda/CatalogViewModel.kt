package com.example.aplicaciontienda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CatalogUiState {
    object Loading : CatalogUiState()
    data class Success(val colegios: List<ColegioUI>) : CatalogUiState()
    data class Error(val message: String) : CatalogUiState()
}

class CatalogViewModel(private val repository: CatalogRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    fun loadCatalog() {
        // Solo cargar si está en estado Loading o Error para evitar recargas innecesarias
        if (_uiState.value is CatalogUiState.Success) return

        viewModelScope.launch {
            try {
                val listaColegios = repository.getCatalogData()
                _uiState.emit(CatalogUiState.Success(listaColegios))
            } catch (e: Exception) {
                _uiState.emit(CatalogUiState.Error(e.message ?: "Error al cargar el catálogo"))
            }
        }
    }
}

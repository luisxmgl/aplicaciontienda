package com.example.aplicaciontienda.data.catalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicaciontienda.CatalogRepository
import com.example.aplicaciontienda.ColegioUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CatalogUiState {
    object Loading : CatalogUiState
    data class Ready(val colegios: List<ColegioUI>) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

/** Carga el catálogo una sola vez y lo comparte entre todas las pantallas (evita releer/parsear el JSON en cada navegación). */
class CatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CatalogRepository(application)

    private val _state = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _state.value = CatalogUiState.Loading
            try {
                val data = repository.getCatalogData()
                _state.value = CatalogUiState.Ready(data)
            } catch (e: Exception) {
                _state.value = CatalogUiState.Error(e.message ?: "Error al cargar el catálogo")
            }
        }
    }
}

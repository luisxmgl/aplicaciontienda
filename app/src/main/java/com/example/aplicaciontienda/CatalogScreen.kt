package com.example.aplicaciontienda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(viewModel: CatalogViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Cargar el catálogo una sola vez al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadCatalog()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Catálogo por Colegios") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is CatalogUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize())
                }
                is CatalogUiState.Success -> {
                    val colegiosPorComuna = state.colegios.groupBy { it.comuna }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        colegiosPorComuna.forEach { (comuna, colegios) ->
                            item {
                                Text(
                                    text = comuna,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(colegios) { colegio ->
                                ColegioItem(colegio)
                            }
                        }
                    }
                }
                is CatalogUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxSize().wrapContentSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ColegioItem(colegio: ColegioUI) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = colegio.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${colegio.productos.size} productos disponibles",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

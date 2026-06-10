package com.example.aplicaciontienda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CatalogScreen(viewModel: CatalogViewModel) {
    // Usamos LaunchedEffect(Unit) para cargar el catálogo una sola vez
    LaunchedEffect(Unit) {
        viewModel.loadCatalog()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is CatalogUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is CatalogUiState.Success -> {
                CatalogList(state.familias, state.productosPorFamilia)
            }
            is CatalogUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(text = "Error: ${state.message}", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun CatalogList(familias: List<Familia>, productosPorFamilia: Map<String, List<Producto>>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recorremos las familias para mostrar sus productos
        items(familias) { familia ->
            val productos = productosPorFamilia[familia.codfamilia] ?: emptyList()
            
            if (productos.isNotEmpty()) {
                FamiliaSection(familia, productos)
            }
        }
    }
}

@Composable
fun FamiliaSection(familia: Familia, productos: List<Producto>) {
    Column {
        Text(
            text = familia.nomfamilia,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        productos.forEach { producto ->
            ProductoItem(producto)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
fun ProductoItem(producto: Producto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = producto.producto, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Precio: $${producto.precioxpublico}", fontSize = 14.sp)
            Text(text = "Stock: ${producto.existencia}", fontSize = 14.sp)
        }
    }
}

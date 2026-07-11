package com.example.aplicaciontienda.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.Producto
import com.example.aplicaciontienda.R
import com.example.aplicaciontienda.Utils
import com.example.aplicaciontienda.data.catalog.CatalogUiState
import com.example.aplicaciontienda.data.catalog.CatalogViewModel
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import com.example.aplicaciontienda.ui.theme.BorderPaleTan
import com.example.aplicaciontienda.ui.theme.VerdeWhatsapp

private const val CATEGORIA_TODOS = "TODOS"
private const val CATEGORIA_MENOR_PRECIO = "Menor Precio"

/** Equivalente a Store.jsx / TiendaActivity: catálogo de un colegio (o de todos), búsqueda y chips de categoría/precio. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    colegioId: String,
    colegioNombre: String,
    catalogViewModel: CatalogViewModel,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    onProductClick: (Producto) -> Unit
) {
    val state by catalogViewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedCategoria by remember { mutableStateOf(CATEGORIA_TODOS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(colegioNombre) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
                actions = {
                    IconButton(onClick = {
                        Utils.openWhatsApp(context, "+56920680021", "Hola! Quería consultar sobre los uniformes de $colegioNombre.")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "WhatsApp", tint = VerdeWhatsapp)
                    }
                    IconButton(onClick = onCartClick) { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            is CatalogUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is CatalogUiState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(s.message) }
            is CatalogUiState.Ready -> {
                val todosLosProductos = remember(s.colegios, colegioId) {
                    if (colegioId == "ALL") s.colegios.flatMap { it.productos }
                    else s.colegios.find { it.id == colegioId }?.productos ?: emptyList()
                }
                val categorias = remember(todosLosProductos) {
                    listOf(CATEGORIA_TODOS, CATEGORIA_MENOR_PRECIO) +
                        todosLosProductos.map { it.nombre.substringBefore(" T-").trim() }.distinct().sorted()
                }

                val filtrados = remember(todosLosProductos, query, selectedCategoria) {
                    var lista = if (query.isBlank()) todosLosProductos
                    else todosLosProductos.filter {
                        it.nombre.lowercase().contains(query.lowercase()) || it.colegio.lowercase().contains(query.lowercase())
                    }
                    lista = when (selectedCategoria) {
                        CATEGORIA_TODOS -> lista
                        CATEGORIA_MENOR_PRECIO -> lista.sortedBy { it.precio }
                        else -> lista.filter { it.nombre.startsWith(selectedCategoria) }
                    }
                    lista
                }

                Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = { Text("Buscar producto…") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
                        items(categorias) { cat ->
                            FilterChip(
                                selected = cat == selectedCategoria,
                                onClick = { selectedCategoria = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentThread.copy(alpha = 0.2f))
                            )
                        }
                    }

                    if (filtrados.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.store_no_products)) }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(filtrados, key = { it.idproducto }) { producto -> ProductCard(producto, onClick = { onProductClick(producto) }) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(producto: Producto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderPaleTan)
    ) {
        Column(Modifier.padding(0.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(com.example.aplicaciontienda.ui.theme.PrimaryInk, com.example.aplicaciontienda.ui.theme.PrimaryInkDark)
                        )
                    )
            )
            Column(Modifier.padding(10.dp)) {
                Text(producto.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Text(Utils.formatPrice(producto.precio), style = MaterialTheme.typography.titleSmall, color = AccentThread, fontWeight = FontWeight.Bold)
            }
        }
    }
}

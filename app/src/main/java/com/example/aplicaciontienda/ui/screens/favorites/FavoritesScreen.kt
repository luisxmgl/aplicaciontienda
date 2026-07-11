package com.example.aplicaciontienda.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.FavoritesManager
import com.example.aplicaciontienda.Producto
import com.example.aplicaciontienda.R
import com.example.aplicaciontienda.Utils
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BorderPaleTan
import com.example.aplicaciontienda.ui.theme.PrimaryInk
import com.example.aplicaciontienda.ui.theme.PrimaryInkDark

/** Pantalla extra de Android (no presente en la web): productos favoritos guardados localmente. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onBack: () -> Unit, onProductClick: (Producto) -> Unit) {
    val favoritos = FavoritesManager.getFavorites()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoritos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        if (favoritos.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes productos favoritos.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoritos) { producto ->
                    Card(onClick = { onProductClick(producto) }, border = androidx.compose.foundation.BorderStroke(1.dp, BorderPaleTan)) {
                        Box(
                            Modifier.fillMaxWidth().height(90.dp)
                                .background(Brush.linearGradient(listOf(PrimaryInk, PrimaryInkDark)))
                        )
                        Column(Modifier.padding(10.dp)) {
                            Text(producto.nombre, maxLines = 2, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(Utils.formatPrice(producto.precio), color = AccentThread, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
        }
    }
}

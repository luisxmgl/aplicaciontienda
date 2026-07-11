package com.example.aplicaciontienda.ui.screens.productdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.CartManager
import com.example.aplicaciontienda.FavoritesManager
import com.example.aplicaciontienda.Producto
import com.example.aplicaciontienda.R
import com.example.aplicaciontienda.Utils
import com.example.aplicaciontienda.data.catalog.CatalogUiState
import com.example.aplicaciontienda.data.catalog.CatalogViewModel
import com.example.aplicaciontienda.data.firebase.BehaviorTracker
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.PrimaryInk
import com.example.aplicaciontienda.ui.theme.PrimaryInkDark
import com.example.aplicaciontienda.ui.theme.VerdeWhatsapp

/** Equivalente a ProductDetail.jsx / ProductDetailActivity (sin Villa Puntos, ya eliminado). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    producto: Producto,
    catalogViewModel: CatalogViewModel,
    onBack: () -> Unit,
    onAddedToCart: () -> Unit,
    onProductClick: (Producto) -> Unit
) {
    val context = LocalContext.current
    val state by catalogViewModel.state.collectAsState()
    var added by remember { mutableStateOf(false) }
    var isFavorite by remember(producto) { mutableStateOf(FavoritesManager.isFavorite(producto)) }

    LaunchedEffect(producto.idproducto) {
        BehaviorTracker.registrarVistaProducto(producto.nombre, producto.precio, mapOf("colegio" to producto.colegio))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(producto.nombre, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
                actions = {
                    IconButton(onClick = {
                        FavoritesManager.toggleFavorite(context, producto)
                        isFavorite = FavoritesManager.isFavorite(producto)
                    }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) AccentThread else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "¡Mira este producto en Villa Acero: ${producto.nombre}! ${Utils.formatPrice(producto.precio)}")
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir producto"))
                    }) { Icon(Icons.Default.Share, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.linearGradient(listOf(PrimaryInk, PrimaryInkDark)), shape = MaterialTheme.shapes.small)
            )
            Spacer(Modifier.height(20.dp))
            Text(producto.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(Utils.formatPrice(producto.precio), style = MaterialTheme.typography.headlineSmall, color = AccentThread, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            if (producto.colegio.isNotEmpty()) {
                Text(producto.colegio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))
            Text(producto.descripcion, style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    CartManager.addItem(producto)
                    BehaviorTracker.registrarAgregarCarrito(producto.nombre, 1, mapOf("colegio" to producto.colegio))
                    added = true
                    onAddedToCart()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentThread)
            ) {
                Text(stringResource(R.string.product_detail_add_to_cart), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    val message = context.getString(
                        R.string.product_detail_whatsapp_query,
                        producto.nombre,
                        producto.talla,
                        Utils.formatPrice(producto.precio)
                    )
                    Utils.openWhatsApp(context, "56920680021", message)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdeWhatsapp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.consult_doubts))
            }

            if (state is CatalogUiState.Ready) {
                val related = remember(state) {
                    (state as CatalogUiState.Ready).colegios
                        .find { it.nombre == producto.colegio }?.productos
                        ?.filter { it.nombre != producto.nombre }
                        ?.take(6) ?: emptyList()
                }
                if (related.isNotEmpty()) {
                    Spacer(Modifier.height(28.dp))
                    Text("Completa el uniforme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(related, key = { it.idproducto }) { relatedProduct ->
                            Card(
                                onClick = { onProductClick(relatedProduct) },
                                modifier = Modifier.width(140.dp)
                            ) {
                                Column(Modifier.padding(10.dp)) {
                                    Text(relatedProduct.nombre, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(4.dp))
                                    Text(Utils.formatPrice(relatedProduct.precio), color = AccentThread, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

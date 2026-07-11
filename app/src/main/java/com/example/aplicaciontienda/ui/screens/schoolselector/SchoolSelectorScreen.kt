package com.example.aplicaciontienda.ui.screens.schoolselector

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.Colegio
import com.example.aplicaciontienda.R
import com.example.aplicaciontienda.Utils
import com.example.aplicaciontienda.data.catalog.CatalogUiState
import com.example.aplicaciontienda.data.catalog.CatalogViewModel
import com.example.aplicaciontienda.ui.components.AppBottomNav
import com.example.aplicaciontienda.ui.components.BottomNavItem
import com.example.aplicaciontienda.ui.components.markOrdersSeen
import com.example.aplicaciontienda.ui.components.rememberAdminBadgeCounts
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import com.example.aplicaciontienda.ui.theme.BorderPaleTan
import com.example.aplicaciontienda.ui.theme.VerdeWhatsapp

/** Equivalente a SchoolSelector.jsx / MainSelectorActivity: lista de colegios con búsqueda y filtro por comuna. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolSelectorScreen(
    esAdmin: Boolean,
    catalogViewModel: CatalogViewModel,
    onBack: () -> Unit,
    onSchoolSelected: (Colegio) -> Unit,
    onSobreNosotros: () -> Unit,
    onCart: () -> Unit = {},
    onMyOrders: () -> Unit = {},
    onTracking: () -> Unit = {},
    onFavorites: () -> Unit = {},
    onChat: () -> Unit = {},
    onNotificationRoute: (String) -> Unit = {},
    onAdminOrders: () -> Unit = {},
    onAdminMessages: () -> Unit = {},
    onAdminDashboard: () -> Unit = {},
    onAdminHr: () -> Unit = {},
    onAdminCaja: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by catalogViewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }
    var selectedComuna by remember { mutableStateOf("Todas") }
    val badges by rememberAdminBadgeCounts(esAdmin)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (esAdmin) stringResource(R.string.school_selector_title_admin)
                        else stringResource(R.string.school_selector_title_guest)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                },
                actions = {
                    if (!esAdmin) {
                        com.example.aplicaciontienda.ui.components.NotificationsBellButton(
                            guestId = com.example.aplicaciontienda.Utils.getUniqueUserId(context),
                            onNavigate = onNotificationRoute
                        )
                    }
                    IconButton(onClick = onSobreNosotros) { Icon(Icons.Default.Info, contentDescription = null) }
                }
            )
        },
        bottomBar = {
            if (!esAdmin) {
                AppBottomNav(
                    listOf(
                        BottomNavItem(Icons.AutoMirrored.Filled.Send, "WhatsApp", tint = VerdeWhatsapp) {
                            Utils.openWhatsApp(context, "+56920680021", "Hola! Quería consultar sobre los uniformes.")
                        },
                        BottomNavItem(Icons.AutoMirrored.Filled.Chat, "Chat", onClick = onChat),
                        BottomNavItem(Icons.Default.Favorite, "Favoritos", onClick = onFavorites),
                        BottomNavItem(Icons.Default.Receipt, "Mis Pedidos", onClick = onMyOrders),
                        BottomNavItem(Icons.Default.LocalShipping, "Seguimiento", onClick = onTracking),
                        BottomNavItem(Icons.Default.ShoppingCart, "Carrito", onClick = onCart)
                    )
                )
            } else {
                AppBottomNav(
                    listOf(
                        BottomNavItem(Icons.AutoMirrored.Filled.Chat, "Mensajes", badge = badges.unreadMessages, onClick = onAdminMessages),
                        BottomNavItem(Icons.Default.Receipt, "Gestión", badge = badges.newOrders, onClick = {
                            markOrdersSeen(context)
                            onAdminOrders()
                        }),
                        BottomNavItem(Icons.Default.PointOfSale, "Caja", onClick = onAdminCaja),
                        BottomNavItem(Icons.Default.BarChart, "Dashboard", onClick = onAdminDashboard),
                        BottomNavItem(Icons.Default.People, "RRHH", onClick = onAdminHr)
                    )
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                placeholder = { Text(stringResource(R.string.school_selector_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            when (val s = state) {
                is CatalogUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                is CatalogUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(s.message) }
                }
                is CatalogUiState.Ready -> {
                    val colegios = remember(s.colegios) {
                        s.colegios.map { ui ->
                            Colegio(id = ui.id, nombre = ui.nombre, comuna = ui.comuna, logo = ui.logo, productos = ui.productos)
                        }
                    }
                    val comunas = remember(colegios) { listOf("Todas") + colegios.map { it.comuna }.distinct().sorted() }

                    LazyRowChips(comunas, selectedComuna) { selectedComuna = it }

                    val filtrados = colegios.filter {
                        (selectedComuna == "Todas" || it.comuna == selectedComuna) &&
                            (it.nombre.lowercase().contains(query.lowercase()) || it.comuna.lowercase().contains(query.lowercase()))
                    }

                    Text(
                        text = "${colegios.size} colegios disponibles en la zona",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (filtrados.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.school_selector_no_results))
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(filtrados) { colegio -> SchoolCard(colegio, onClick = { onSchoolSelected(colegio) }) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyRowChips(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        items(options) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentThread.copy(alpha = 0.2f))
            )
        }
    }
}

@Composable
private fun SchoolCard(colegio: Colegio, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundPaperRaised),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderPaleTan)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(colegio.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(colegio.comuna, style = MaterialTheme.typography.bodySmall)
        }
    }
}

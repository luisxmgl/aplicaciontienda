package com.example.aplicaciontienda.ui.screens.about

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.aplicaciontienda.Colegio
import com.example.aplicaciontienda.Utils
import com.example.aplicaciontienda.data.catalog.CatalogUiState
import com.example.aplicaciontienda.data.catalog.CatalogViewModel
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import kotlinx.coroutines.delay

/** Equivalente a SobreNosotros.jsx / SobreNosotrosActivity: slideshow, contacto y lista de colegios. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SobreNosotrosScreen(
    catalogViewModel: CatalogViewModel,
    onBack: () -> Unit,
    onSchoolSelected: (Colegio) -> Unit,
    onContacto: () -> Unit = {},
    onTerminos: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by catalogViewModel.state.collectAsState()
    val images = remember { listSlideImages(context) }
    val pagerState = rememberPagerState(pageCount = { if (images.isEmpty()) 0 else Int.MAX_VALUE })

    LaunchedEffect(images) {
        if (images.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(2200)
            val next = (pagerState.currentPage + 1)
            pagerState.animateScrollToPage(next)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sobre Nosotros") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            if (images.isNotEmpty()) {
                item {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(200.dp)) { page ->
                        AsyncImage(
                            model = "file:///android_asset/slide/${images[page % images.size]}",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            item {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Confecciones Villa Acero fabrica uniformes escolares a medida para colegios de Hualpén, Concepción, Talcahuano y Chiguayante.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = {
                            Utils.openWhatsApp(context, "+56920680021", "Hola! Quería consultar sobre los uniformes.")
                        }) { Text("WhatsApp") }
                        OutlinedButton(onClick = {
                            Utils.openInstagram(context, "https://www.instagram.com/confecciones.villaacero/")
                        }) { Text("Instagram") }
                        OutlinedButton(onClick = {
                            Utils.openGoogleMaps(context, "Confecciones Villa Acero, Los Poetas 8741, Hualpén")
                        }) { Text("Cómo llegar") }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("Colegios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            if (state is CatalogUiState.Ready) {
                val colegios = (state as CatalogUiState.Ready).colegios
                    .map { Colegio(id = it.id, nombre = it.nombre, comuna = it.comuna, logo = it.logo, productos = it.productos) }
                    .sortedBy { it.nombre }
                items(colegios, key = { it.id }) { colegio ->
                    Card(
                        onClick = { onSchoolSelected(colegio) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundPaperRaised)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(colegio.nombre, fontWeight = FontWeight.SemiBold)
                            Text(colegio.comuna, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(onClick = onContacto) { Text("Contacto") }
                    TextButton(onClick = onTerminos) { Text("Términos y Condiciones") }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun listSlideImages(context: Context): List<String> =
    try {
        context.assets.list("slide")?.filter { it.endsWith(".jpg") || it.endsWith(".jpeg") }?.sorted() ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

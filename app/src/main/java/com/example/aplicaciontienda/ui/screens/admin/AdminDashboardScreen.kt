package com.example.aplicaciontienda.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.Pedido
import com.example.aplicaciontienda.Utils
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BackgroundPaperRaised
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

private data class DashboardData(
    val pedidos: List<Pedido> = emptyList(),
    val abandonedCount: Int = 0,
    val productViews: Map<String, Int> = emptyMap(),
    val pageViews: Map<String, Int> = emptyMap()
)

/** Equivalente a AdminDashboard.jsx: métricas de ventas/pendientes/abandonos/top productos, sin librería de gráficos (igual que la web). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onBack: () -> Unit, onVerPedidos: () -> Unit) {
    var data by remember { mutableStateOf(DashboardData()) }

    DisposableEffect(Unit) {
        val pedidosRef = FirebaseDatabase.getInstance().getReference("pedidos")
        val abandonedRef = FirebaseDatabase.getInstance().getReference("abandoned_carts")
        val behaviorRef = FirebaseDatabase.getInstance().getReference("behavior_events")

        val pedidosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Pedido::class.java) }
                data = data.copy(pedidos = list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        val abandonedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { data = data.copy(abandonedCount = snapshot.childrenCount.toInt()) }
            override fun onCancelled(error: DatabaseError) {}
        }
        val behaviorListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val views = mutableMapOf<String, Int>()
                val pages = mutableMapOf<String, Int>()
                for (child in snapshot.children) {
                    val tipo = child.child("tipo").getValue(String::class.java) ?: continue
                    when (tipo) {
                        "product_view" -> {
                            val producto = child.child("producto").getValue(String::class.java) ?: continue
                            views[producto] = (views[producto] ?: 0) + 1
                        }
                        "page_view" -> {
                            val pagina = child.child("pagina").getValue(String::class.java) ?: continue
                            pages[pagina] = (pages[pagina] ?: 0) + 1
                        }
                    }
                }
                data = data.copy(productViews = views, pageViews = pages)
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        pedidosRef.addValueEventListener(pedidosListener)
        abandonedRef.addValueEventListener(abandonedListener)
        behaviorRef.addValueEventListener(behaviorListener)

        onDispose {
            pedidosRef.removeEventListener(pedidosListener)
            abandonedRef.removeEventListener(abandonedListener)
            behaviorRef.removeEventListener(behaviorListener)
        }
    }

    val startOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val sevenDaysAgo = remember { System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000 }

    // Los pedidos con Webpay se guardan como pagado=false apenas se abre el flujo de pago;
    // si el cliente se arrepiente o no completa el pago, ese registro nunca se confirma y no
    // debe contar como venta real.
    val pedidosPagados = remember(data.pedidos) { data.pedidos.filter { it.metodoPago != "webpay" || it.pagado == true } }
    val ventasHoy = pedidosPagados.filter { it.fecha >= startOfToday }.sumOf { it.total + it.extraCharge }
    val pedidosHoy = pedidosPagados.count { it.fecha >= startOfToday }
    val porPreparar = pedidosPagados.count { it.estado <= 2 }
    val ventasSemana = pedidosPagados.filter { it.fecha >= sevenDaysAgo }.sumOf { it.total + it.extraCharge }

    val topProductos = remember(pedidosPagados) {
        pedidosPagados.filter { it.fecha >= sevenDaysAgo }
            .flatMap { it.items }
            .groupBy { it.nombre }
            .mapValues { it.value.sumOf { i -> i.cantidad } }
            .toList().sortedByDescending { it.second }.take(3)
    }
    val topColegios = remember(pedidosPagados) {
        pedidosPagados.filter { it.fecha >= sevenDaysAgo }
            .flatMap { it.items }
            .filter { it.colegio.isNotEmpty() }
            .groupBy { it.colegio }
            .mapValues { it.value.sumOf { i -> i.cantidad } }
            .toList().sortedByDescending { it.second }.take(3)
    }
    val topVistos = remember(data.productViews) { data.productViews.toList().sortedByDescending { it.second }.take(3) }
    val topPaginas = remember(data.pageViews) { data.pageViews.toList().sortedByDescending { it.second }.take(3) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard("Ventas hoy", Utils.formatPrice(ventasHoy), "$pedidosHoy pedidos", Modifier.weight(1f))
                    SummaryCard("Por preparar", porPreparar.toString(), "pedidos pendientes", Modifier.weight(1f), highlight = porPreparar > 0)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard("Ventas semana", Utils.formatPrice(ventasSemana), "últimos 7 días", Modifier.weight(1f))
                    SummaryCard("Carritos sin finalizar", data.abandonedCount.toString(), "abandonados", Modifier.weight(1f))
                }
            }
            if (porPreparar > 0) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = AccentThread.copy(alpha = 0.15f))) {
                        Row(
                            Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Hay pedidos esperando preparación",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            TextButton(
                                onClick = onVerPedidos,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) { Text("Ver pedidos") }
                        }
                    }
                }
            }
            item { RankedSection("Lo que más se vende (7 días)", topProductos) }
            item { RankedSection("Lo que más miran", topVistos) }
            item { RankedSection("Colegios con más demanda (7 días)", topColegios) }
            item { RankedSection("Páginas más vistas", topPaginas) }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, sub: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = if (highlight) AccentThread.copy(alpha = 0.2f) else BackgroundPaperRaised)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(sub, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RankedSection(title: String, items: List<Pair<String, Int>>) {
    Card {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (items.isEmpty()) {
                Text("Sin datos todavía.", style = MaterialTheme.typography.bodySmall)
            } else {
                items.forEachIndexed { index, (name, count) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${index + 1}. $name", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        Text(count.toString(), fontWeight = FontWeight.Bold, color = AccentThread)
                    }
                }
            }
        }
    }
}

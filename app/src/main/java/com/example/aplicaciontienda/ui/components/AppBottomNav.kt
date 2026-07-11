package com.example.aplicaciontienda.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.BorderPaleTan
import com.example.aplicaciontienda.ui.theme.VerdeWhatsapp

data class BottomNavItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val badge: Int = 0,
    val tint: Color? = null,
    val onClick: () -> Unit
)

/** Equivalente al .bottom-nav flotante de GlobalLayout.jsx: barra inferior con ítems distintos para invitado/admin. */
@Composable
fun AppBottomNav(items: List<BottomNavItem>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, BorderPaleTan)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item -> BottomNavButton(item) }
        }
    }
}

@Composable
private fun BottomNavButton(item: BottomNavItem) {
    Box {
        IconButton(onClick = item.onClick) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = item.tint ?: MaterialTheme.colorScheme.primary
            )
        }
        if (item.badge > 0) {
            Badge(
                containerColor = AccentThread,
                modifier = Modifier.align(Alignment.TopEnd)
            ) { Text(if (item.badge > 9) "9+" else item.badge.toString()) }
        }
    }
}

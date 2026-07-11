package com.example.aplicaciontienda.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Radios pequeños tipo "sastrería" en tarjetas/botones/inputs (4-6dp), como en styles.css.
// Los pills/chips/badges usan RoundedCornerShape(50) directamente donde se necesitan.
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

val PillShape = RoundedCornerShape(50)

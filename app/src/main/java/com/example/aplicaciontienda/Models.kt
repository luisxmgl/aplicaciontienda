package com.example.aplicaciontienda

import java.io.Serializable

// Estas clases han sido movidas a archivos individuales Colegio.kt y Producto.kt
// para evitar duplicación y conflictos.
// data class Producto(...)
// data class Colegio(...)

data class CatalogoMetadata(
    val titulo: String,
    val total_productos: Int,
    val total_colegios: Int
)

data class Catalogo(
    val metadata: CatalogoMetadata,
    val colegios: List<Colegio>
)

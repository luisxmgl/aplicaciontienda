package com.example.aplicaciontienda

data class Colegio(
    val id: Int,
    val nombre: String,
    val comuna: String,
    val direccion: String = "",
    val logo: String = "",
    val productos: List<Producto> = emptyList()
)
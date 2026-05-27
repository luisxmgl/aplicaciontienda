package com.example.aplicaciontienda

import java.io.Serializable

data class Producto(
    val nombre: String,
    val talla: String,
    val precio: Int,
    val colegio: String,
    var cantidad: Int = 0
) : Serializable
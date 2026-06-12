package com.example.aplicaciontienda

data class Pedido(
    val id: String = "",
    val codigoRetiro: String = "",
    val items: List<CartItemPedido> = emptyList(),
    val total: Int = 0,
    val extraCharge: Int = 0,
    val customization: String = "",
    val estado: Int = 1, // 1: Recibido, 2: Confección/Bordado, 3: Listo para Retiro, 4: Entregado
    val fecha: Long = System.currentTimeMillis(),
    val procesado: Boolean = false
)

data class CartItemPedido(
    val nombre: String = "",
    val talla: String = "",
    val precio: Int = 0,
    val cantidad: Int = 0,
    val colegio: String = ""
)

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
    val procesado: Boolean = false,
    // Campos compartidos con la web (pedidos online con Webpay y ventas de Caja/POS)
    val email: String? = null,
    val metodoPago: String? = null, // "webpay" | "efectivo" | "tarjeta" | "transferencia"
    val pagado: Boolean? = null,
    val origen: String? = null, // "pos" para ventas de Caja; null/ausente para pedidos online
    val montoRecibido: Int? = null,
    val vuelto: Int? = null,
    val cajero: String? = null,
    val pagoEstado: String? = null, // "aprobado" | "rechazado" (Webpay)
    val webpayAuthorizationCode: String? = null,
    val webpayResponseCode: Int? = null
)

data class CartItemPedido(
    val nombre: String = "",
    val talla: String = "",
    val precio: Int = 0,
    val cantidad: Int = 0,
    val colegio: String = ""
)

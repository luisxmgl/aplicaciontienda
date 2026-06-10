package com.example.aplicaciontienda

data class CartItem(
    val producto: Producto,
    var cantidad: Int
)

object CartManager {
    private val items = mutableListOf<CartItem>()

    fun addItem(producto: Producto) {
        val existing = items.find { it.producto.idproducto == producto.idproducto }
        if (existing != null) {
            existing.cantidad++
        } else {
            items.add(CartItem(producto, 1))
        }
    }

    fun removeItem(cartItem: CartItem) {
        items.remove(cartItem)
    }

    fun updateQuantity(cartItem: CartItem, delta: Int) {
        val newQty = cartItem.cantidad + delta
        if (newQty > 0) {
            cartItem.cantidad = newQty
        } else {
            removeItem(cartItem)
        }
    }

    fun getItems(): List<CartItem> = items

    fun getTotal(): Int {
        return items.sumOf { (it.producto.precio * it.cantidad) }
    }

    fun clear() {
        items.clear()
    }
}
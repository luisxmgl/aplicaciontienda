package com.example.aplicaciontienda.ui.nav

/** Rutas de navegación Compose — mapean 1:1 a las rutas de src/App.jsx de la web. */
object Routes {
    const val HOME = "home"
    const val SCHOOL_SELECTOR = "colegios/{esAdmin}"
    const val STORE = "tienda/{colegioId}/{esAdmin}"
    const val PRODUCT_DETAIL = "producto"
    const val CART = "carrito"
    const val MY_ORDERS = "mis-pedidos"
    const val TRACKING = "seguimiento"
    const val FAVORITES = "favoritos"
    const val CHAT = "chat?esAdmin={esAdmin}&targetId={targetId}"
    const val ADMIN_LOGIN = "admin/login"
    const val ADMIN_ORDERS = "admin/pedidos"
    const val ADMIN_MESSAGES = "admin/mensajes"
    const val ADMIN_DASHBOARD = "admin/dashboard"
    const val ADMIN_HR = "admin/rrhh"
    const val ADMIN_CAJA = "admin/caja"
    const val WEBPAY = "webpay/{orderCode}/{amount}"
    const val PAGO_RESULTADO = "pago-resultado/{orderCode}/{estado}"
    const val SOBRE_NOSOTROS = "sobre-nosotros"
    const val CONTACTO = "contacto"
    const val TERMINOS = "terminos-y-condiciones"

    fun schoolSelector(esAdmin: Boolean) = "colegios/$esAdmin"
    fun store(colegioId: String, esAdmin: Boolean) = "tienda/$colegioId/$esAdmin"
    fun chat(esAdmin: Boolean, targetId: String? = null) = "chat?esAdmin=$esAdmin&targetId=${targetId ?: ""}"
    fun webpay(orderCode: String, amount: Int) = "webpay/$orderCode/$amount"
    fun pagoResultado(orderCode: String, estado: String) = "pago-resultado/$orderCode/$estado"
}

/**
 * Sostiene el producto seleccionado para pasar a ProductDetailScreen sin serializar
 * el objeto completo por la nav graph (mismo patrón que CartManager para el carrito).
 */
object SelectedProductHolder {
    var producto: com.example.aplicaciontienda.Producto? = null
}

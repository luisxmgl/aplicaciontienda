package cl.villaacero.uniformes.data

import kotlinx.serialization.Serializable

/**
 * Estructura raíz del catálogo cargado desde res/raw/catalogo.json
 */
@Serializable
data class Catalogo(
    val negocio: Negocio,
    val colegios: List<Colegio>
)

/**
 * Información del negocio (Confecciones Villa Acero).
 */
@Serializable
data class Negocio(
    val nombre: String,
    val whatsapp: String,
    val instagram: String,
    val instagramUrl: String,
    val zonaAtencion: String
)

/**
 * Un colegio con su catálogo de productos.
 */
@Serializable
data class Colegio(
    val id: Int,
    val nombre: String,
    val comuna: String,
    val direccion: String,
    val productos: List<Producto>
)

/**
 * Un producto individual (una talla específica de un tipo de prenda en un colegio).
 * Esta es la estructura "plana" como viene del POS.
 */
@Serializable
data class Producto(
    val tipoPrenda: String,
    val talla: String,
    val precio: Int,
    val stock: Int
)

/**
 * Productos agrupados por tipo de prenda (vista para la UI).
 * Se construye combinando varios [Producto] que comparten tipoPrenda.
 */
data class ProductoAgrupado(
    val tipoPrenda: String,
    val tallas: List<TallaPrecio>,
    val precioMin: Int,
    val precioMax: Int,
    val tieneStock: Boolean
)

/**
 * Una talla con su precio (para mostrar la tabla en la pantalla de detalle).
 */
data class TallaPrecio(
    val talla: String,
    val precio: Int,
    val stock: Int
)

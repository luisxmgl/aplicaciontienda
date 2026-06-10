package cl.villaacero.uniformes.data

internal val xlPattern = Regex("""^(\d+)XL$""")

/**
 * Devuelve una clave numérica para ordenar tallas en el orden visual esperado:
 * numéricas (2, 4, 6...) → letras (S, M, L, XL, 2XL, 3XL...) → ÚNICA.
 */
internal fun tallaSortKey(t: String): Int = when {
    t == "ÚNICA" || t == "UNICA" -> 9999
    t.toIntOrNull() != null -> t.toInt()
    t == "S" -> 200
    t == "M" -> 201
    t == "L" -> 202
    t == "XL" -> 203
    xlPattern.matches(t) -> 200 + xlPattern.find(t)!!.groupValues[1].toInt()
    else -> 5000
}

/**
 * Agrupa productos por tipo de prenda. Cada grupo contiene todas las tallas
 * disponibles ordenadas, y el rango de precios.
 */
fun agruparProductosPorTipo(productos: List<Producto>): List<ProductoAgrupado> {
    if (productos.isEmpty()) return emptyList()

    return productos
        .groupBy { it.tipoPrenda }
        .map { (tipo, lista) ->
            val tallas = lista
                .map { TallaPrecio(it.talla, it.precio, it.stock) }
                .sortedBy { tallaSortKey(it.talla) }
            ProductoAgrupado(
                tipoPrenda = tipo,
                tallas = tallas,
                precioMin = lista.minOf { it.precio },
                precioMax = lista.maxOf { it.precio },
                tieneStock = lista.any { it.stock > 0 }
            )
        }
        .sortedBy { it.tipoPrenda }
}

/**
 * Lista de tipos de prenda únicos en los productos, ordenada.
 */
fun tiposDePrendaUnicos(productos: List<Producto>): List<String> =
    productos.map { it.tipoPrenda }.distinct().sorted()

/**
 * Lista de comunas únicas en los colegios, ordenada.
 */
fun comunasUnicas(colegios: List<Colegio>): List<String> =
    colegios.map { it.comuna }.distinct().sorted()

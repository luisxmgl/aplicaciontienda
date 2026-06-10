package com.example.aplicaciontienda

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CatalogData(
    @SerializedName("familias") val familias: List<Familia>,
    @SerializedName("productos") val productos: List<Producto>
)

data class Familia(
    val codfamilia: String,
    val nomfamilia: String
) : Serializable

data class Producto(
    val idproducto: String,
    val codproducto: String,
    val producto: String,
    val codfamilia: String,
    val nomfamilia: String,
    val precioxpublico: String,
    val precioxmayor: String? = null,
    val precioxmenor: String? = null,
    val existencia: String,
    val codigobarra: String? = null,
    val ivaproducto: String? = null,
    val estado: String? = null
) : Serializable {
    // Propiedades de compatibilidad para corregir errores de referencia
    val nombre: String get() = producto
    val colegio: String get() = nomfamilia
    val precio: Int get() = precioxpublico.toDoubleOrNull()?.toInt() ?: 0
    val stock: Int get() = existencia.toDoubleOrNull()?.toInt() ?: 0
    
    // Intenta extraer la talla del nombre del producto (ej: "POLERON T-10" -> "10")
    val talla: String get() {
        return when {
            producto.contains(" T-") -> producto.substringAfterLast(" T-").trim()
            producto.contains(" TALLA ") -> producto.substringAfterLast(" TALLA ").trim()
            else -> "Única"
        }
    }

    // Constructor de compatibilidad para evitar errores en código antiguo
    constructor(nombre: String, talla: String, precio: Int, colegio: String, comuna: String, stock: Int) : this(
        idproducto = "",
        codproducto = "",
        producto = if (talla == "Única") nombre else "$nombre T-$talla",
        codfamilia = "",
        nomfamilia = colegio,
        precioxpublico = precio.toString(),
        precioxmayor = "0.00",
        precioxmenor = "0.00",
        existencia = stock.toString(),
        codigobarra = "",
        ivaproducto = "SI",
        estado = "1"
    )
}

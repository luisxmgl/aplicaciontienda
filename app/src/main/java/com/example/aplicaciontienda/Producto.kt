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
) : Serializable {
    val comuna: String get() = when (nomfamilia.trim().uppercase()) {
        "SAGRADOS CORAZONES", "PRESTON", "VILLA ACERO", "HIGH SCOPE" -> "Hualpén"
        "BAUTISTA", "INMACULADA", "CONCEPCION-PEDRO DE VALDIVIA", "KINGSTON COLLEGE" -> "Concepción"
        "ITAHUE", "PINARES" -> "Chiguayante"
        "STA. LEONOR", "SAN CRISTOBAL", "TJS" -> "Talcahuano"
        else -> "Desconocida"
    }
}

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
    val precio: Int get() = precioxpublico.toDoubleOrNull()?.toInt() ?: 0
    val stock: Int get() = existencia.toDoubleOrNull()?.toInt() ?: 0
    
    val nombre: String get() {
        val index = producto.indexOf(" TALLA ", ignoreCase = true)
        return if (index != -1) {
            producto.substring(0, index).trim()
        } else {
            producto.trim()
        }
    }

    val talla: String get() {
        val index = producto.indexOf(" TALLA ", ignoreCase = true)
        return if (index != -1) {
            producto.substring(index + 7).trim()
        } else {
            "N/A"
        }
    }

    val colegio: String get() = nomfamilia.trim()
}

data class ColegioUI(
    val id: String,
    val nombre: String,
    val comuna: String,
    val logo: String,
    val productos: List<Producto>
)

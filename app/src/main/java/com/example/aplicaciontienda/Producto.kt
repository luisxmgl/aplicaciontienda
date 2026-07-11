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
    val producto: String,
    val precioxpublico: String,
    val existencia: String,
    val codproducto: String? = null,
    val codfamilia: String? = null,
    val nomfamilia: String? = null,
    val precioxmayor: String? = null,
    val precioxmenor: String? = null,
    val codigobarra: String? = null,
    val ivaproducto: String? = null,
    val estado: String? = null
) : Serializable {
    val precio: Int get() = precioxpublico.toDoubleOrNull()?.toInt() ?: 0
    val stock: Int get() = existencia.toDoubleOrNull()?.toInt() ?: 0
    
    val nombre: String get() = producto.trim()

    val talla: String get() {
        val nameUpper = nombre.uppercase()
        val index = nameUpper.indexOf(" TALLA ")
        if (index != -1) return nombre.substring(index + 7).trim()
        
        // También buscar " T-" que es común
        val indexT = nameUpper.indexOf(" T-")
        if (indexT != -1) return nombre.substring(indexT + 3).trim()

        return "N/A"
    }

    val colegio: String get() = nomfamilia?.trim() ?: ""

    val descripcion: String get() = Utils.generateDescription(nombre)
}

data class ColegioUI(
    val id: String,
    val nombre: String,
    val comuna: String,
    val logo: String,
    val productos: List<Producto>
)

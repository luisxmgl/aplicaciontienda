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
    
    val nombre: String get() = producto.trim()

    val talla: String get() {
        val index = producto.indexOf(" TALLA ", ignoreCase = true)
        return if (index != -1) {
            producto.substring(index + 7).trim()
        } else {
            "N/A"
        }
    }

    val colegio: String get() = nomfamilia.trim()

    val descripcion: String get() {
        val name = producto.uppercase()
        return when {
            name.contains("POLERON") -> "Polerón de alta calidad con interior de franela suave, ideal para mantener la temperatura corporal durante los días fríos. Cuenta con costuras reforzadas en hombros y cuello para una mayor durabilidad ante el uso diario. Tela de composición mixta que asegura que no encoja ni pierda su color original tras los lavados."
            name.contains("POLERA") -> "Polera confeccionada en tela altamente respirable y cómoda, ideal para el movimiento constante. Cuello y puños con tejido reforzado que mantiene su forma lavado tras lavado. Costuras planas para evitar roces molestos y asegurar el confort durante toda la jornada escolar."
            name.contains("BUZO") || name.contains("PANTALON") -> "Prenda fabricada en tela de alta resistencia al roce y al uso intenso. Cuenta con pretina elástica reforzada para un ajuste seguro y cómodo. El tejido permite una excelente libertad de movimiento, siendo ideal tanto para clases normales como para educación física."
            name.contains("CASACA") -> "Casaca térmica con capa exterior repelente a la humedad y forro interior abrigado. Posee cierres de alta calidad y puños ajustables para proteger contra el viento y el frío. Diseño ergonómico pensado para la máxima comodidad del estudiante."
            name.contains("FALDA") || name.contains("JUMPER") -> "Confeccionado en tela de sarga de alta resistencia con terminaciones prolijas. El material es resistente a las arrugas y mantiene el color firme por mucho más tiempo. Diseño clásico con calce cómodo para el uso diario prolongado."
            name.contains("PARKA") -> "Parka impermeable de alto rendimiento con aislamiento térmico superior. Protege eficazmente contra la lluvia y el viento, manteniendo al estudiante seco y temperado. Incluye bolsillos funcionales y terminaciones reforzadas."
            else -> "Uniforme de alta calidad, confeccionado con telas resistentes y duraderas. Diseñado específicamente para cumplir con las exigencias del uso escolar diario, ofreciendo un equilibrio perfecto entre comodidad, durabilidad y una excelente presentación personal."
        }
    }
}

data class ColegioUI(
    val id: String,
    val nombre: String,
    val comuna: String,
    val logo: String,
    val productos: List<Producto>
)

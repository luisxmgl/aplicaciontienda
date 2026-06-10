package cl.villaacero.uniformes.navigation

import android.net.Uri

/**
 * Rutas de navegación de la app.
 *
 * Nota: tipoPrenda puede contener "/" (ej. "POLERA M/L"), por eso usamos Uri.encode
 * al construir la ruta. Navigation Compose decodifica automáticamente al leer el argumento.
 */
sealed class Screen(val route: String) {

    data object Home : Screen("home")

    data object Catalogo : Screen("catalogo/{colegioId}") {
        const val ARG_COLEGIO_ID = "colegioId"
        fun routeFor(colegioId: Int): String = "catalogo/$colegioId"
    }

    data object Detalle : Screen("detalle/{colegioId}/{tipoPrenda}") {
        const val ARG_COLEGIO_ID = "colegioId"
        const val ARG_TIPO_PRENDA = "tipoPrenda"
        fun routeFor(colegioId: Int, tipoPrenda: String): String =
            "detalle/$colegioId/${Uri.encode(tipoPrenda)}"
    }

    data object Nosotros : Screen("nosotros")
}

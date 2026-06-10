package cl.villaacero.uniformes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.villaacero.uniformes.ui.catalogo.CatalogoScreen
import cl.villaacero.uniformes.ui.detalle.DetalleScreen
import cl.villaacero.uniformes.ui.home.HomeScreen
import cl.villaacero.uniformes.ui.nosotros.NosotrosScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onColegioClick = { colegioId ->
                    navController.navigate(Screen.Catalogo.routeFor(colegioId))
                },
                onAboutClick = {
                    navController.navigate(Screen.Nosotros.route)
                }
            )
        }

        composable(
            route = Screen.Catalogo.route,
            arguments = listOf(
                navArgument(Screen.Catalogo.ARG_COLEGIO_ID) { type = NavType.IntType }
            )
        ) {
            CatalogoScreen(
                onBack = { navController.popBackStack() },
                onProductoClick = { colegioId, tipoPrenda ->
                    navController.navigate(Screen.Detalle.routeFor(colegioId, tipoPrenda))
                }
            )
        }

        composable(
            route = Screen.Detalle.route,
            arguments = listOf(
                navArgument(Screen.Detalle.ARG_COLEGIO_ID) { type = NavType.IntType },
                navArgument(Screen.Detalle.ARG_TIPO_PRENDA) { type = NavType.StringType }
            )
        ) {
            DetalleScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Nosotros.route) {
            NosotrosScreen(
                onBack = { navController.popBackStack() },
                onColegioClick = { colegioId ->
                    navController.navigate(Screen.Catalogo.routeFor(colegioId))
                }
            )
        }
    }
}

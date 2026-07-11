package com.example.aplicaciontienda.ui.nav

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aplicaciontienda.Colegio
import com.example.aplicaciontienda.data.catalog.CatalogViewModel
import com.example.aplicaciontienda.ui.screens.about.SobreNosotrosScreen
import com.example.aplicaciontienda.ui.screens.admin.AdminChatListScreen
import com.example.aplicaciontienda.ui.screens.admin.AdminDashboardScreen
import com.example.aplicaciontienda.ui.screens.admin.AdminHRScreen
import com.example.aplicaciontienda.ui.screens.admin.AdminLoginScreen
import com.example.aplicaciontienda.ui.screens.admin.AdminOrdersScreen
import com.example.aplicaciontienda.ui.screens.admin.CajaScreen
import com.example.aplicaciontienda.ui.screens.cart.CartScreen
import com.example.aplicaciontienda.ui.screens.chat.ChatScreen
import com.example.aplicaciontienda.ui.screens.contacto.ContactoScreen
import com.example.aplicaciontienda.ui.screens.favorites.FavoritesScreen
import com.example.aplicaciontienda.ui.screens.home.HomeScreen
import com.example.aplicaciontienda.ui.screens.legal.TerminosScreen
import com.example.aplicaciontienda.ui.screens.myorders.MyOrdersScreen
import com.example.aplicaciontienda.ui.screens.productdetail.ProductDetailScreen
import com.example.aplicaciontienda.ui.screens.schoolselector.SchoolSelectorScreen
import com.example.aplicaciontienda.ui.screens.store.StoreScreen
import com.example.aplicaciontienda.ui.screens.tracking.TrackingScreen
import com.example.aplicaciontienda.ui.screens.webpay.PagoResultadoScreen
import com.example.aplicaciontienda.ui.screens.webpay.WebpayScreen

@Composable
fun AppNavHost(startRoute: String? = null) {
    val navController = rememberNavController()
    val catalogViewModel: CatalogViewModel = viewModel()

    NavHost(navController = navController, startDestination = startRoute ?: Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onGuestEntry = { navController.navigate(Routes.schoolSelector(false)) },
                onAdminEntry = { navController.navigate(Routes.ADMIN_LOGIN) }
            )
        }

        composable(Routes.ADMIN_LOGIN) {
            AdminLoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.schoolSelector(true)) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_ORDERS) {
            AdminOrdersScreen(
                onBack = { navController.popBackStack() },
                onOpenCaja = { navController.navigate(Routes.ADMIN_CAJA) }
            )
        }

        composable(Routes.ADMIN_MESSAGES) {
            AdminChatListScreen(
                onBack = { navController.popBackStack() },
                onOpenChat = { chatId -> navController.navigate(Routes.chat(true, chatId)) }
            )
        }

        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onBack = { navController.popBackStack() },
                onVerPedidos = { navController.navigate(Routes.ADMIN_ORDERS) }
            )
        }

        composable(Routes.ADMIN_HR) {
            AdminHRScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_CAJA) {
            CajaScreen(catalogViewModel = catalogViewModel, onBack = { navController.popBackStack() })
        }

        composable(
            Routes.SCHOOL_SELECTOR,
            arguments = listOf(navArgument("esAdmin") { type = NavType.BoolType })
        ) { backStackEntry ->
            val esAdmin = backStackEntry.arguments?.getBoolean("esAdmin") ?: false
            SchoolSelectorScreen(
                esAdmin = esAdmin,
                catalogViewModel = catalogViewModel,
                onBack = { navController.popBackStack() },
                onSchoolSelected = { colegio: Colegio ->
                    navController.navigate(Routes.store(colegio.id, esAdmin))
                },
                onSobreNosotros = { navController.navigate(Routes.SOBRE_NOSOTROS) },
                onCart = { navController.navigate(Routes.CART) },
                onMyOrders = { navController.navigate(Routes.MY_ORDERS) },
                onTracking = { navController.navigate(Routes.TRACKING) },
                onFavorites = { navController.navigate(Routes.FAVORITES) },
                onChat = { navController.navigate(Routes.chat(esAdmin)) },
                onNotificationRoute = { route -> navController.navigate(route) },
                onAdminOrders = { navController.navigate(Routes.ADMIN_ORDERS) },
                onAdminMessages = { navController.navigate(Routes.ADMIN_MESSAGES) },
                onAdminDashboard = { navController.navigate(Routes.ADMIN_DASHBOARD) },
                onAdminHr = { navController.navigate(Routes.ADMIN_HR) },
                onAdminCaja = { navController.navigate(Routes.ADMIN_CAJA) }
            )
        }

        composable(
            Routes.STORE,
            arguments = listOf(
                navArgument("colegioId") { type = NavType.StringType },
                navArgument("esAdmin") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val colegioId = backStackEntry.arguments?.getString("colegioId") ?: "ALL"
            StoreScreen(
                colegioId = colegioId,
                colegioNombre = "Tienda",
                catalogViewModel = catalogViewModel,
                onBack = { navController.popBackStack() },
                onCartClick = { navController.navigate(Routes.CART) },
                onProductClick = { producto ->
                    SelectedProductHolder.producto = producto
                    navController.navigate(Routes.PRODUCT_DETAIL)
                }
            )
        }

        composable(Routes.PRODUCT_DETAIL) {
            val producto = SelectedProductHolder.producto
            if (producto != null) {
                ProductDetailScreen(
                    producto = producto,
                    catalogViewModel = catalogViewModel,
                    onBack = { navController.popBackStack() },
                    onAddedToCart = { navController.popBackStack() },
                    onProductClick = { related ->
                        SelectedProductHolder.producto = related
                        navController.navigate(Routes.PRODUCT_DETAIL)
                    }
                )
            }
        }

        composable(Routes.CART) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onContinueShopping = { navController.popBackStack() },
                onNavigateToWebpay = { orderCode, amount -> navController.navigate(Routes.webpay(orderCode, amount)) }
            )
        }

        composable(Routes.MY_ORDERS) {
            MyOrdersScreen(
                onBack = { navController.popBackStack() },
                onOrderClick = { code -> navController.navigate("${Routes.TRACKING}?code=$code") }
            )
        }

        composable(
            "${Routes.TRACKING}?code={code}",
            arguments = listOf(navArgument("code") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            TrackingScreen(
                initialCode = backStackEntry.arguments?.getString("code"),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FAVORITES) {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onProductClick = { producto ->
                    SelectedProductHolder.producto = producto
                    navController.navigate(Routes.PRODUCT_DETAIL)
                }
            )
        }

        composable(
            Routes.CHAT,
            arguments = listOf(
                navArgument("esAdmin") { type = NavType.BoolType; defaultValue = false },
                navArgument("targetId") { type = NavType.StringType; nullable = true; defaultValue = "" }
            )
        ) { backStackEntry ->
            val esAdmin = backStackEntry.arguments?.getBoolean("esAdmin") ?: false
            val targetId = backStackEntry.arguments?.getString("targetId")
            ChatScreen(esAdmin = esAdmin, targetId = targetId, onBack = { navController.popBackStack() })
        }

        composable(
            Routes.WEBPAY,
            arguments = listOf(
                navArgument("orderCode") { type = NavType.StringType },
                navArgument("amount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val orderCode = backStackEntry.arguments?.getString("orderCode") ?: ""
            val amount = backStackEntry.arguments?.getInt("amount") ?: 0
            WebpayScreen(
                orderCode = orderCode,
                amount = amount,
                onResult = { code, estado ->
                    navController.navigate(Routes.pagoResultado(code.ifEmpty { orderCode }, estado)) {
                        popUpTo(Routes.CART) { inclusive = false }
                    }
                },
                onError = { message ->
                    Log.e("WebpayFlow", "Fallo al iniciar pago para pedido $orderCode: $message")
                    navController.navigate(Routes.pagoResultado(orderCode, "error")) {
                        popUpTo(Routes.CART) { inclusive = false }
                    }
                }
            )
        }

        composable(
            Routes.PAGO_RESULTADO,
            arguments = listOf(
                navArgument("orderCode") { type = NavType.StringType },
                navArgument("estado") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderCode = backStackEntry.arguments?.getString("orderCode") ?: ""
            val estado = backStackEntry.arguments?.getString("estado") ?: "error"
            PagoResultadoScreen(
                orderCode = orderCode,
                estado = estado,
                onVerPedido = { navController.navigate("${Routes.TRACKING}?code=$orderCode") },
                onVolverInicio = {
                    navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                }
            )
        }

        composable(Routes.SOBRE_NOSOTROS) {
            SobreNosotrosScreen(
                catalogViewModel = catalogViewModel,
                onBack = { navController.popBackStack() },
                onSchoolSelected = { colegio -> navController.navigate(Routes.store(colegio.id, false)) },
                onContacto = { navController.navigate(Routes.CONTACTO) },
                onTerminos = { navController.navigate(Routes.TERMINOS) }
            )
        }

        composable(Routes.CONTACTO) {
            ContactoScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.TERMINOS) {
            TerminosScreen(onBack = { navController.popBackStack() })
        }
    }
}

package cl.villaacero.uniformes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cl.villaacero.uniformes.data.CatalogRepository
import cl.villaacero.uniformes.navigation.AppNavigation
import cl.villaacero.uniformes.ui.theme.ConfeccionesVillaAceroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() debe llamarse ANTES de super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Cargar el catálogo desde res/raw/catalogo.json
        CatalogRepository.init(applicationContext)

        setContent {
            ConfeccionesVillaAceroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

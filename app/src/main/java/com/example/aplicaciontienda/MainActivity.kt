package com.example.aplicaciontienda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aplicaciontienda.ui.nav.AppNavHost
import com.example.aplicaciontienda.ui.theme.VillaAceroTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        FavoritesManager.init(this)
        PresenceManager.updateLastSeen(Utils.getUniqueUserId(this))

        val startRoute = intent.getStringExtra(EXTRA_START_ROUTE)

        enableEdgeToEdge()
        setContent {
            VillaAceroTheme {
                AppNavHost(startRoute = startRoute)
            }
        }
    }

    companion object {
        /** Ruta Compose inicial opcional, usada al volver desde Activities XML aún no portadas (Admin/SobreNosotros). */
        const val EXTRA_START_ROUTE = "START_ROUTE"
    }
}

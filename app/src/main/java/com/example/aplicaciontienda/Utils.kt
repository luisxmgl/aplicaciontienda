package com.example.aplicaciontienda

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.NumberFormat
import java.util.Locale

object Utils {
    fun getLogoForColegio(nombre: String): String {
        val normalized = nombre.uppercase().trim()
        return when {
            normalized.contains("BAUTISTA") -> "logo_bautista"
            normalized.contains("HIGH SCOPE") -> "logo_highscope"
            normalized.contains("INMACULADA") -> "logo_inmaculada"
            normalized.contains("ITAHUE") -> "logocolegioitahue"
            normalized.contains("KINGSTON") -> "logo_kingstoncollege"
            normalized.contains("PINARES") -> "logopinares"
            normalized.contains("PRESTON") -> "logo_preston"
            normalized.contains("SAGRADOS CORAZONES") -> "logo_sagradoscorazones"
            normalized.contains("SAN CRISTOBAL") -> "logo_sancristobal"
            normalized.contains("LEONOR") -> "logo_santaleonor"
            normalized.contains("TJS") || normalized.contains("THOMAS") -> "logo_thomasjeffersonschool"
            normalized.contains("VILLA ACERO") -> "logo_villaacero"
            normalized.contains("CONCEPCION") || normalized.contains("VALDIVIA") -> "logoconcepcion_pedrodevaldivia"
            else -> ""
        }
    }

    fun formatPrice(precio: Int): String {
        val nf = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        nf.maximumFractionDigits = 0
        return nf.format(precio)
    }

    fun openWhatsApp(context: Context, phone: String, message: String) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val encodedMessage = Uri.encode(message)
        val whatsappUrl = "https://wa.me/$cleanPhone?text=$encodedMessage"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show()
        }
    }

    fun openInstagram(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Instagram no instalado", Toast.LENGTH_SHORT).show()
        }
    }
}

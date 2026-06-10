package cl.villaacero.uniformes.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.NumberFormat
import java.util.Locale

/**
 * Formato chileno de precio: 11100 -> "$11.100"
 */
fun formatPrice(precio: Int): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    nf.maximumFractionDigits = 0
    return nf.format(precio)
}

/**
 * Abre WhatsApp con un mensaje pre-armado.
 */
fun openWhatsApp(context: Context, phone: String, message: String) {
    val cleanPhone = phone.replace(Regex("[^0-9]"), "")
    val encodedMessage = Uri.encode(message)
    val whatsappUrl = "https://wa.me/$cleanPhone?text=$encodedMessage"

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No se pudo abrir WhatsApp. ¿Está instalado?",
            Toast.LENGTH_LONG
        ).show()
    }
}

/**
 * Abre Instagram (app o navegador como fallback).
 */
fun openInstagram(context: Context, instagramUrl: String) {
    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl)).apply {
        setPackage("com.instagram.android")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(appIntent)
        return
    } catch (e: ActivityNotFoundException) {
        // Fallback al navegador
    }

    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(webIntent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No se pudo abrir Instagram", Toast.LENGTH_LONG).show()
    }
}

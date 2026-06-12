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

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateOrderCode(): String {
        val date = java.text.SimpleDateFormat("ddMM", Locale.getDefault()).format(java.util.Date())
        val random = (1000..9999).random().toString()
        // Mezclamos un poco la fecha con el random para los 4 dígitos
        // Por ejemplo: primer dígito del día + primer dígito del mes + 2 random
        val d1 = date[0]
        val m1 = date[2]
        val r1 = random[0]
        val r2 = random[1]
        return "$d1$m1$r1$r2"
    }

    fun generateQRCode(text: String, size: Int): android.graphics.Bitmap? {
        return try {
            val bitMatrix = com.google.zxing.qrcode.QRCodeWriter().encode(
                text,
                com.google.zxing.BarcodeFormat.QR_CODE,
                size,
                size
            )
            val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun openGoogleMaps(context: Context, address: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            // Fallback to browser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}"))
            context.startActivity(webIntent)
        }
    }
}

package com.example.aplicaciontienda

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.NumberFormat
import java.util.Locale

object Utils {
    private const val ORDER_ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"

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
        val sb = StringBuilder()
        for (i in 0 until 4) {
            sb.append(ORDER_ALPHABET.random())
        }
        return "$date-${sb.toString()}"
    }

    fun generateDescription(productName: String): String {
        val name = productName.uppercase()
        return when {
            name.contains("POLERON") -> "Polerón de alta calidad, interior de franela suave y costuras reforzadas para mayor durabilidad."
            name.contains("POLERA") -> "Polera confeccionada en tela respirable, ideal para el uso diario con cuello reforzado."
            name.contains("PANTALON") || name.contains("BUZO") -> "Prenda resistente a la fricción, con cintura elástica ajustable para máxima comodidad."
            name.contains("CASACA") || name.contains("PARKA") -> "Aislamiento térmico superior y tela repelente al agua, perfecta para días fríos."
            else -> "Uniforme de alta calidad confeccionado con materiales seleccionados para garantizar resistencia y comodidad durante todo el año escolar."
        }
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

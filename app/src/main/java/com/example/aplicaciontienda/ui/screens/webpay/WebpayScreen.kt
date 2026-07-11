package com.example.aplicaciontienda.ui.screens.webpay

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.compose.ui.viewinterop.AndroidView
import com.example.aplicaciontienda.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * Flujo real de Webpay Plus vía WebView, contra el mismo servidor Express que usa la web
 * (server.js + server/webpay.js, sin cambios). Ver BuildConfig.WEBPAY_BASE_URL.
 */
@Composable
fun WebpayScreen(orderCode: String, amount: Int, onResult: (orderCode: String, estado: String) -> Unit, onError: (String) -> Unit) {
    var formHtml by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orderCode, amount) {
        try {
            // OkHttp .execute() es bloqueante: debe correr en un dispatcher de fondo, nunca
            // directamente en la coroutine de LaunchedEffect (lanza NetworkOnMainThreadException).
            val html = withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val body = JSONObject().apply {
                    put("orderCode", orderCode)
                    put("amount", amount)
                }.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("${BuildConfig.WEBPAY_BASE_URL}/api/webpay/create")
                    .post(body)
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("No se pudo iniciar el pago (código ${response.code}). Verifica que el servidor esté disponible.")
                    }
                    val json = JSONObject(response.body?.string() ?: "{}")
                    val token = json.getString("token")
                    val url = json.getString("url")
                    """
                        <html><body onload="document.forms[0].submit()">
                        <form method="POST" action="$url">
                        <input type="hidden" name="token_ws" value="$token" />
                        </form>
                        </body></html>
                    """.trimIndent()
                }
            }
            formHtml = html
        } catch (e: IOException) {
            Log.e("WebpayScreen", "Error de conexión al iniciar Webpay", e)
            errorMessage = "No se pudo conectar con el servidor de pagos (${BuildConfig.WEBPAY_BASE_URL}). ${e.message ?: ""}"
        } catch (e: Exception) {
            Log.e("WebpayScreen", "Error inesperado al iniciar Webpay", e)
            errorMessage = e.message ?: "Error desconocido al iniciar Webpay"
        }
    }

    errorMessage?.let {
        LaunchedEffect(it) { onError(it) }
    }

    val html = formHtml
    if (html == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (errorMessage == null) CircularProgressIndicator() else Text(errorMessage!!)
        }
    } else {
        WebpayWebView(html = html, onResult = onResult)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebpayWebView(html: String, onResult: (orderCode: String, estado: String) -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: android.webkit.WebResourceRequest): Boolean {
                        val url = request.url
                        if (url.toString().contains("pago-resultado")) {
                            val code = url.getQueryParameter("orderCode") ?: ""
                            val estado = url.getQueryParameter("estado") ?: "error"
                            onResult(code, estado)
                            return true
                        }
                        return false
                    }
                }
                loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        }
    )
}

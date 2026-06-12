package com.example.aplicaciontienda

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class CartActivity : AppCompatActivity() {

    private lateinit var tvTotal: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvPointsInfo: TextView
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarCart)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvCart = findViewById<RecyclerView>(R.id.rvCart)
        rvCart.layoutManager = LinearLayoutManager(this)
        
        tvTotal = findViewById(R.id.tvCartTotal)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvPointsInfo = findViewById(R.id.tvPointsInfo)
        
        adapter = CartAdapter(CartManager.getItems()) {
            actualizarTotal()
        }
        rvCart.adapter = adapter
        
        actualizarTotal()

        findViewById<android.view.View>(R.id.btnClearCart).setOnClickListener {
            if (CartManager.getItems().isNotEmpty()) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Vaciar carrito")
                    .setMessage("¿Estás seguro de que quieres eliminar todos los productos del carrito?")
                    .setPositiveButton("Sí") { _, _ ->
                        CartManager.clear()
                        adapter.notifyDataSetChanged()
                        actualizarTotal()
                        Toast.makeText(this, "Carrito vaciado", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        findViewById<MaterialButton>(R.id.btnCheckout).setOnClickListener {
            val items = CartManager.getItems()
            if (items.isNotEmpty()) {
                showChatbot { extraCharge, customization ->
                    val totalFinal = CartManager.getTotal() + extraCharge
                    val orderCode = Utils.generateOrderCode()
                    val sb = StringBuilder("¡Hola! Me gustaría realizar el siguiente pedido:\n\n")
                    sb.append("CÓDIGO DE RETIRO: $orderCode\n")
                    sb.append("------------------------------\n")
                    items.forEach { item ->
                        sb.append("• ${item.producto.nombre}\n")
                        sb.append("  Cantidad: ${item.cantidad}\n")
                        if (item.producto.colegio.isNotEmpty()) {
                            sb.append("  Colegio: ${item.producto.colegio}\n")
                        }
                        sb.append("\n")
                    }
                    
                    if (customization.isNotEmpty()) {
                        sb.append("Detalles de Ajuste: $customization\n")
                        sb.append("Cargo extra: ${Utils.formatPrice(extraCharge)}\n\n")
                    }
                    
                    sb.append("Total final: ${Utils.formatPrice(totalFinal)}\n\n")
                    sb.append("*Nota: Tengo 3 días hábiles para cualquier cambio de opinión sobre los ajustes.*")
                    
                    Utils.openWhatsApp(this, "+56920680021", sb.toString())
                    CartManager.clear()
                    PointsManager.addPoints(this, PointsManager.calculatePoints(totalFinal))
                    showSuccessDialog(orderCode)
                }
            } else {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnWebpay).setOnClickListener {
            if (CartManager.getItems().isNotEmpty()) {
                showChatbot { extraCharge, customization ->
                    val orderCode = Utils.generateOrderCode()
                    val webpayUrl = "https://www.webpay.cl" 
                    Utils.openUrl(this, webpayUrl)
                    
                    PointsManager.addPoints(this, PointsManager.calculatePoints(CartManager.getTotal()))
                    CartManager.clear()
                    showSuccessDialog(orderCode)
                }
            } else {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<android.view.View>(R.id.tvFaqLink).setOnClickListener {
            showFaqDialog()
        }

        findViewById<android.view.View>(R.id.btnThemeToggle).setOnClickListener {
            toggleDarkMode()
        }

        findViewById<android.view.View>(R.id.btnGoShopping)?.setOnClickListener {
            finish() // Volver a la tienda
        }
    }

    private fun toggleDarkMode() {
        val currentMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        if (currentMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun showFaqDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_faq, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<android.view.View>(R.id.btnCloseFaq).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<android.view.View>(R.id.btnOpenMap)?.setOnClickListener {
            Utils.openGoogleMaps(this, "Tienda Villa Acero, Hualpén")
        }

        dialog.show()
    }

    private fun showSuccessDialog(orderCode: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_chatbot, null)
        val llChat = dialogView.findViewById<android.widget.LinearLayout>(R.id.llChatContainer)
        val llOptions = dialogView.findViewById<android.widget.LinearLayout>(R.id.llOptionsContainer)
        val svChat = dialogView.findViewById<androidx.core.widget.NestedScrollView>(R.id.svChat)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        fun addMessage(text: String, isBot: Boolean) {
            val tv = TextView(this).apply {
                this.text = text
                this.setPadding(30, 20, 30, 20)
                this.layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.setMargins(if (isBot) 0 else 60, 10, if (isBot) 60 else 0, 10)
                    this.gravity = if (isBot) android.view.Gravity.START else android.view.Gravity.END
                }
                this.setBackgroundResource(if (isBot) R.drawable.bg_message_received else R.drawable.bg_message_sent)
                this.setTextColor(if (isBot) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
            llChat.addView(tv)
            svChat.post { svChat.fullScroll(android.view.View.FOCUS_DOWN) }
        }

        addMessage("¡Pedido realizado con éxito! 🎉", true)
        addMessage("Tu código de retiro es: $orderCode", true)
        
        // Add a copy button for the code
        val btnCopy = MaterialButton(this).apply {
            this.text = "COPIAR CÓDIGO"
            this.textSize = 12f
            this.layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { 
                this.gravity = android.view.Gravity.CENTER
                this.setMargins(0, 0, 0, 20)
            }
            this.setOnClickListener {
                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Código de Retiro", orderCode)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@CartActivity, "Código copiado", Toast.LENGTH_SHORT).show()
            }
        }
        llChat.addView(btnCopy)

        addMessage("Muéstrale este QR a la cajera al retirar:", true)

        val qrBitmap = Utils.generateQRCode(orderCode, 500)
        if (qrBitmap != null) {
            val ivQr = android.widget.ImageView(this).apply {
                this.setImageBitmap(qrBitmap)
                this.layoutParams = android.widget.LinearLayout.LayoutParams(500, 500).apply {
                    this.gravity = android.view.Gravity.CENTER
                    this.setMargins(0, 20, 0, 20)
                }
            }
            llChat.addView(ivQr)
        }

        fun showRating() {
            llOptions.removeAllViews()
            addMessage("¿Qué tal fue tu experiencia hoy?", true)
            val starsLayout = android.widget.LinearLayout(this).apply {
                this.orientation = android.widget.LinearLayout.HORIZONTAL
                this.gravity = android.view.Gravity.CENTER
            }
            for (i in 1..5) {
                val btnStar = android.widget.Button(this).apply {
                    this.text = "⭐"
                    this.textSize = 20f
                    this.background = null
                    this.layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    this.setOnClickListener {
                        llOptions.removeAllViews()
                        addMessage("¡Gracias por calificar con $i estrellas! ❤️", true)
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            dialog.dismiss()
                            finish()
                        }, 2000)
                    }
                }
                starsLayout.addView(btnStar)
            }
            llOptions.addView(starsLayout)
        }

        val btnOk = MaterialButton(this).apply {
            this.text = "ENTENDIDO"
            this.setOnClickListener { showRating() }
        }
        llOptions.addView(btnOk)

        dialog.show()
    }

    private var finalCustomization = ""
    private var finalExtraCharge = 0
    private val customizations = mutableListOf<String>()

    private fun showChatbot(onComplete: (Int, String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_chatbot, null)
        val llChat = dialogView.findViewById<android.widget.LinearLayout>(R.id.llChatContainer)
        val llOptions = dialogView.findViewById<android.widget.LinearLayout>(R.id.llOptionsContainer)
        val svChat = dialogView.findViewById<androidx.core.widget.NestedScrollView>(R.id.svChat)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        fun scrollToBottom() {
            svChat.post { svChat.fullScroll(android.view.View.FOCUS_DOWN) }
        }

        fun addMessage(text: String, isBot: Boolean, delay: Long = 0) {
            val action = {
                val tv = TextView(this).apply {
                    this.text = text
                    this.setPadding(40, 24, 40, 24)
                    this.layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        this.setMargins(if (isBot) 0 else 80, 16, if (isBot) 80 else 0, 16)
                        this.gravity = if (isBot) android.view.Gravity.START else android.view.Gravity.END
                    }
                    this.setBackgroundResource(if (isBot) R.drawable.bg_message_received else R.drawable.bg_message_sent)
                    this.setTextColor(if (isBot) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                    this.textSize = 16f
                    this.elevation = 4f
                    this.setLineSpacing(0f, 1.1f)
                    
                    // Simple fade-in animation
                    this.alpha = 0f
                    this.animate().alpha(1f).setDuration(400).start()
                }
                llChat.addView(tv)
                scrollToBottom()
            }

            if (delay > 0 && isBot) {
                val typingTv = TextView(this).apply {
                    this.text = "Asistente escribiendo..."
                    this.textSize = 13f
                    this.setPadding(40, 0, 0, 0)
                    this.setTextColor(android.graphics.Color.GRAY)
                    this.setTypeface(null, android.graphics.Typeface.ITALIC)
                }
                llChat.addView(typingTv)
                scrollToBottom()
                
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    llChat.removeView(typingTv)
                    action()
                }, delay)
            } else {
                action()
            }
        }

        fun showStep(question: String, options: List<Pair<String, () -> Unit>>, botDelay: Long = 1000) {
            llOptions.removeAllViews()
            addMessage(question, true, botDelay)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                options.forEachIndexed { index, (text, action) ->
                    val btn = MaterialButton(this).apply {
                        this.text = text
                        this.isAllCaps = false
                        this.textSize = 15f
                        this.cornerRadius = (24 * resources.displayMetrics.density).toInt()
                        this.setPadding(32, 24, 32, 24)
                        this.layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 12, 0, 12) }
                        
                        this.setBackgroundColor(android.graphics.Color.WHITE)
                        this.setTextColor(android.graphics.Color.parseColor("#1A456F"))
                        this.strokeWidth = (1.5 * resources.displayMetrics.density).toInt()
                        this.strokeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1A456F"))
                        this.stateListAnimator = null // Remove shadow for cleaner look
                        
                        // Entrance animation for buttons
                        this.translationY = 50f
                        this.alpha = 0f
                        this.animate().translationY(0f).alpha(1f).setDuration(300).setStartDelay(index * 100L).start()
                        
                        this.setOnClickListener {
                            llOptions.removeAllViews()
                            addMessage(text, false)
                            action()
                        }
                    }
                    llOptions.addView(btn)
                    scrollToBottom()
                }
            }, botDelay + 300)
        }

        fun stepFinished() {
            llOptions.removeAllViews()
            finalCustomization = customizations.joinToString(", ")
            val summary = if (finalExtraCharge > 0) 
                "¡Excelente! He configurado tus ajustes: $finalCustomization.\n\nEl total se actualizará con un cargo de ${Utils.formatPrice(finalExtraCharge)} por el trabajo adicional."
                else "Entendido. Procesaremos tu pedido con las medidas estándar de fábrica."
            
            addMessage(summary, true, 1200)
            addMessage("¿Deseas finalizar el pedido ahora?", true, 2000)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val btnConfirm = MaterialButton(this).apply {
                    this.text = "✅ SÍ, FINALIZAR PEDIDO"
                    this.textSize = 17f
                    this.setPadding(0, 32, 0, 32)
                    this.cornerRadius = (16 * resources.displayMetrics.density).toInt()
                    this.setBackgroundColor(android.graphics.Color.parseColor("#28A745"))
                    this.setTextColor(android.graphics.Color.WHITE)
                    this.layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    this.setOnClickListener {
                        dialog.dismiss()
                        onComplete(finalExtraCharge, finalCustomization)
                    }
                }
                llOptions.addView(btnConfirm)
                scrollToBottom()
            }, 2500)
        }

        // --- Flujo Amigable y Profesional ---
        finalCustomization = ""
        finalExtraCharge = 0
        customizations.clear()

        fun stepDeadline() {
            showStep("Para coordinar con el taller, ¿cuándo necesitas tener tu pedido en mano?", listOf(
                "📅 Lo antes posible" to {
                    customizations.add("Prioridad normal")
                    stepFinished()
                },
                "🎁 Es para un regalo (Esta semana)" to {
                    customizations.add("Urgencia: Para esta semana")
                    stepFinished()
                },
                "🗓️ No tengo prisa (Próxima semana)" to {
                    customizations.add("Sin urgencia")
                    stepFinished()
                }
            ))
        }

        fun stepGiftWrapping() {
            showStep("¿Te gustaría que envolvamos tu pedido para regalo? 🎀 (Sin costo adicional)", listOf(
                "🎁 Sí, por favor" to {
                    customizations.add("Envolver para regalo")
                    stepDeadline()
                },
                "❌ No es necesario" to {
                    stepDeadline()
                }
            ))
        }

        fun stepSizeConfirmation() {
            showStep("Una pregunta importante: ¿Estás seguro de la talla elegida? (Si tienes dudas, podemos asesorarte al retirar)", listOf(
                "✅ Sí, la talla es correcta" to {
                    customizations.add("Talla confirmada")
                    stepGiftWrapping()
                },
                "❓ No estoy seguro, prefiero asesoría" to {
                    customizations.add("Requiere asesoría de talla en tienda")
                    stepGiftWrapping()
                }
            ))
        }

        fun stepEmbroidery() {
            showStep("¿Te gustaría agregar el nombre bordado del alumno? 🧵 (Valor: $1.000)", listOf(
                "🧵 Sí, agregar bordado" to {
                    finalExtraCharge += 1000
                    showStep("¿En qué parte de la prenda prefieres el bordado?", listOf(
                        "Pecho Izquierdo" to {
                            customizations.add("Bordado en pecho izquierdo")
                            stepSizeConfirmation()
                        },
                        "Pecho Derecho" to {
                            customizations.add("Bordado en pecho derecho")
                            stepSizeConfirmation()
                        },
                        "En la Espalda / Cuello" to {
                            customizations.add("Bordado en espalda/cuello")
                            stepSizeConfirmation()
                        }
                    ))
                },
                "No, sin bordado" to {
                    stepSizeConfirmation()
                }
            ))
        }

        fun stepAdjustments() {
            showStep("¿Necesitas que hagamos algún ajuste de medida? 👖 (Cada ajuste tiene un valor de $1.000)", listOf(
                "👖 Ajuste de Basta / Largo" to {
                    finalExtraCharge += 1000
                    customizations.add("Ajuste de basta")
                    stepEmbroidery()
                },
                "👖 Corte más Ajustado (Slim)" to {
                    finalExtraCharge += 1000
                    customizations.add("Corte Slim")
                    stepEmbroidery()
                },
                "🧥 Ajustar largo de Mangas" to {
                    finalExtraCharge += 1000
                    customizations.add("Ajuste de mangas")
                    stepEmbroidery()
                },
                "✨ Sin ajustes, solo confirmar detalles" to {
                    stepEmbroidery()
                }
            ))
        }

        showStep("¡Hola! Soy tu Asistente Villa Acero. 😊\n\nEstoy aquí para que tu pedido sea perfecto. ¿Te gustaría personalizar tu ropa con ajustes o bordados?", listOf(
            "🌟 Sí, me interesa" to {
                stepAdjustments()
            },
            "📏 Solo confirmar tallas/regalo" to {
                stepSizeConfirmation()
            },
            "🚀 No, comprar estándar" to {
                stepFinished()
            }
        ), 600)

        dialog.show()
    }


    private fun actualizarTotal() {
        val total = CartManager.getTotal()
        tvTotal.text = Utils.formatPrice(total)
        tvSubtotal.text = Utils.formatPrice(total)
        
        val points = PointsManager.calculatePoints(total)
        tvPointsInfo.text = "Ganarás $points puntos con esta compra"

        val emptyView = findViewById<android.view.View>(R.id.llEmptyCart)
        val rvCart = findViewById<android.view.View>(R.id.rvCart)
        val cardCheckout = findViewById<android.view.View>(R.id.cardCheckout)
        val cardLoyalty = findViewById<android.view.View>(R.id.cardLoyalty)

        if (CartManager.getItems().isEmpty()) {
            emptyView?.visibility = android.view.View.VISIBLE
            rvCart?.visibility = android.view.View.GONE
            cardCheckout?.visibility = android.view.View.GONE
            cardLoyalty?.visibility = android.view.View.GONE
        } else {
            emptyView?.visibility = android.view.View.GONE
            rvCart?.visibility = android.view.View.VISIBLE
            cardCheckout?.visibility = android.view.View.VISIBLE
            cardLoyalty?.visibility = android.view.View.VISIBLE
        }
    }
}
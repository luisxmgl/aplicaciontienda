package com.example.aplicaciontienda

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.toColorInt
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

        findViewById<View>(R.id.btnClearCart).setOnClickListener {
            if (CartManager.getItems().isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.clear_cart_title)
                    .setMessage(R.string.clear_cart_message)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        val count = CartManager.getItems().size
                        CartManager.clear()
                        adapter.notifyItemRangeRemoved(0, count)
                        actualizarTotal()
                        Toast.makeText(this, R.string.cart_cleared, Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(R.string.no, null)
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

                    // Guardar localmente primero para asegurar que aparezca en "Mis Pedidos"
                    LocalOrdersManager.saveOrder(this, orderCode)
                    
                    try {
                        saveOrderToFirebase(orderCode, items, CartManager.getTotal(), extraCharge, customization)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    val pointsEarned = PointsManager.calculateTotalPoints(items)
                    PointsManager.addPoints(this, pointsEarned)
                    
                    Utils.openWhatsApp(this, "+56920680021", sb.toString())
                    
                    CartManager.clear()
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
                    val webpayUrl = "https://www.webpay.cl/form-pay/392941" 
                    
                    // Guardar localmente primero para asegurar que aparezca en "Mis Pedidos"
                    LocalOrdersManager.saveOrder(this, orderCode)
                    
                    try {
                        saveOrderToFirebase(orderCode, CartManager.getItems(), CartManager.getTotal(), extraCharge, customization)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    Utils.openUrl(this, webpayUrl)
                    
                    PointsManager.addPoints(this, PointsManager.calculateTotalPoints(CartManager.getItems()))
                    CartManager.clear()
                    showSuccessDialog(orderCode)
                }
            } else {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.tvFaqLink).setOnClickListener {
            showFaqDialog()
        }

        findViewById<View>(R.id.btnGoShopping)?.setOnClickListener {
            finish() // Volver a la tienda
        }
    }


    private fun showFaqDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_faq, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<View>(R.id.btnCloseFaq).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnOpenMap)?.setOnClickListener {
            Utils.openGoogleMaps(this, "Tienda Villa Acero, Hualpén")
        }

        dialog.show()
    }

    private fun showSuccessDialog(orderCode: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_chatbot, null)
        val llChat = dialogView.findViewById<LinearLayout>(R.id.llChatContainer)
        val llOptions = dialogView.findViewById<LinearLayout>(R.id.llOptionsContainer)
        val svChat = dialogView.findViewById<androidx.core.widget.NestedScrollView>(R.id.svChat)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        fun addMessage(text: String, isBot: Boolean) {
            val tv = TextView(this).apply {
                this.text = text
                this.setPadding(30, 20, 30, 20)
                this.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.setMargins(if (isBot) 0 else 60, 10, if (isBot) 60 else 0, 10)
                    this.gravity = if (isBot) Gravity.START else Gravity.END
                }
                this.setBackgroundResource(if (isBot) R.drawable.bg_message_received else R.drawable.bg_message_sent)
                this.setTextColor(if (isBot) Color.BLACK else Color.WHITE)
            }
            llChat.addView(tv)
            svChat.post { svChat.fullScroll(View.FOCUS_DOWN) }
        }

        addMessage(getString(R.string.order_success), true)
        addMessage(getString(R.string.order_code_is, orderCode), true)
        
        // Add a copy button for the code
        val btnCopy = MaterialButton(this).apply {
            this.setText(R.string.copy_code)
            this.textSize = 12f
            this.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { 
                this.gravity = Gravity.CENTER
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

        addMessage(getString(R.string.show_qr_to_cashier), true)

        val qrBitmap = Utils.generateQRCode(orderCode, 500)
        if (qrBitmap != null) {
            val ivQr = ImageView(this).apply {
                this.setImageBitmap(qrBitmap)
                this.layoutParams = LinearLayout.LayoutParams(500, 500).apply {
                    this.gravity = Gravity.CENTER
                    this.setMargins(0, 20, 0, 20)
                }
            }
            llChat.addView(ivQr)
        }

        fun showRating() {
            llOptions.removeAllViews()
            addMessage(getString(R.string.experience_rating), true)
            val starsLayout = LinearLayout(this).apply {
                this.orientation = LinearLayout.HORIZONTAL
                this.gravity = Gravity.CENTER
            }
            for (i in 1..5) {
                val btnStar = android.widget.Button(this).apply {
                    this.text = "⭐"
                    this.textSize = 20f
                    this.background = null
                    this.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    this.setOnClickListener {
                        llOptions.removeAllViews()
                        addMessage(getString(R.string.rating_thanks, i), true)
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
            this.setText(R.string.understood)
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
        val llChat = dialogView.findViewById<LinearLayout>(R.id.llChatContainer)
        val llOptions = dialogView.findViewById<LinearLayout>(R.id.llOptionsContainer)
        val svChat = dialogView.findViewById<androidx.core.widget.NestedScrollView>(R.id.svChat)

        val dialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        fun scrollToBottom() {
            svChat.post { svChat.fullScroll(View.FOCUS_DOWN) }
        }

        fun addMessage(text: String, isBot: Boolean, delay: Long = 0) {
            val action = {
                val tv = TextView(this).apply {
                    this.text = text
                    this.setPadding(40, 24, 40, 24)
                    this.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        this.setMargins(if (isBot) 0 else 80, 16, if (isBot) 80 else 0, 16)
                        this.gravity = if (isBot) Gravity.START else Gravity.END
                    }
                    this.setBackgroundResource(if (isBot) R.drawable.bg_message_received else R.drawable.bg_message_sent)
                    this.setTextColor(if (isBot) Color.BLACK else Color.WHITE)
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
                    this.text = getString(R.string.asistente_escribiendo)
                    this.textSize = 13f
                    this.setPadding(40, 0, 0, 0)
                    this.setTextColor(Color.GRAY)
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
                        this.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 12, 0, 12) }
                        
                        this.setBackgroundColor(Color.WHITE)
                        this.setTextColor("#1A456F".toColorInt())
                        this.strokeWidth = (1.5 * resources.displayMetrics.density).toInt()
                        this.strokeColor = android.content.res.ColorStateList.valueOf("#1A456F".toColorInt())
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
                getString(R.string.chatbot_summary_extra, finalCustomization, Utils.formatPrice(finalExtraCharge))
                else getString(R.string.chatbot_summary_standard)
            
            addMessage(summary, true, 1200)
            addMessage(getString(R.string.finalize_order_question), true, 2000)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val btnConfirm = MaterialButton(this).apply {
                    this.setText(R.string.confirm_finalize_order)
                    this.textSize = 17f
                    this.setPadding(0, 32, 0, 32)
                    this.cornerRadius = (16 * resources.displayMetrics.density).toInt()
                    this.setBackgroundColor("#28A745".toColorInt())
                    this.setTextColor(Color.WHITE)
                    this.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
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
                dialog.dismiss()
                onComplete(0, "")
            }
        ), 600)

        dialog.show()
    }


    private fun saveOrderToFirebase(code: String, items: List<CartItem>, total: Int, extra: Int, cust: String) {
        val database = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("pedidos")
        
        val itemPedido = items.map { 
            CartItemPedido(
                nombre = it.producto.nombre,
                talla = it.producto.talla,
                precio = it.producto.precio,
                cantidad = it.cantidad,
                colegio = it.producto.colegio
            )
        }
        
        val pedido = Pedido(
            id = code,
            codigoRetiro = code,
            items = itemPedido,
            total = total,
            extraCharge = extra,
            customization = cust,
            estado = 1
        )
        
        database.child(code).setValue(pedido).addOnFailureListener {
            Toast.makeText(this, "Error al sincronizar con el servidor", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarTotal() {
        val total = CartManager.getTotal()
        tvTotal.text = Utils.formatPrice(total)
        tvSubtotal.text = Utils.formatPrice(total)
        
        val points = PointsManager.calculateTotalPoints(CartManager.getItems())
        tvPointsInfo.text = getString(R.string.points_gain_info, points)

        val emptyView = findViewById<View>(R.id.llEmptyCart)
        val rvCart = findViewById<View>(R.id.rvCart)
        val cardCheckout = findViewById<View>(R.id.cardCheckout)
        val cardLoyalty = findViewById<View>(R.id.cardLoyalty)

        if (CartManager.getItems().isEmpty()) {
            emptyView?.visibility = View.VISIBLE
            rvCart?.visibility = View.GONE
            cardCheckout?.visibility = View.GONE
            cardLoyalty?.visibility = View.GONE
        } else {
            emptyView?.visibility = View.GONE
            rvCart?.visibility = View.VISIBLE
            cardCheckout?.visibility = View.VISIBLE
            cardLoyalty?.visibility = View.VISIBLE
        }
    }
}
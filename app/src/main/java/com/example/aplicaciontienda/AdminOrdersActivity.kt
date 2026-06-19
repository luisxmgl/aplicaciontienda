package com.example.aplicaciontienda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AdminOrdersActivity : AppCompatActivity() {

    private lateinit var adapter: AdminOrdersAdapter
    private val ordersList = mutableListOf<Pedido>()
    private var databaseRef: DatabaseReference? = null
    private var eventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_admin_orders)
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
            return
        }

        val rvOrders = findViewById<RecyclerView>(R.id.rvAdminOrders)
        if (rvOrders != null) {
            rvOrders.layoutManager = LinearLayoutManager(this)
            adapter = AdminOrdersAdapter(ordersList)
            rvOrders.adapter = adapter
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAdmin)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.title = "Gestión de Pedidos"
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        findViewById<View>(R.id.btnLogout)?.setOnClickListener {
            finish()
        }

        fetchOrders()
    }

    private fun fetchOrders() {
        try {
            val database = FirebaseDatabase.getInstance()
            databaseRef = database.getReference("pedidos")
            
            eventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ordersList.clear()
                    for (postSnapshot in snapshot.children) {
                        try {
                            val pedido = postSnapshot.getValue(Pedido::class.java)
                            if (pedido != null) {
                                ordersList.add(pedido)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    ordersList.sortByDescending { it.fecha }
                    if (::adapter.isInitialized) {
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminOrdersActivity, "Error Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
            databaseRef?.addValueEventListener(eventListener!!)
        } catch (e: Exception) {
            e.printStackTrace()
            // Si llegamos aquí, es probablemente porque Firebase no está inicializado (falta google-services.json)
            Toast.makeText(this, "Firebase no configurado correctamente. Por favor, asegúrese de agregar el archivo google-services.json", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            eventListener?.let { databaseRef?.removeEventListener(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class AdminOrdersAdapter(private val orders: List<Pedido>) : RecyclerView.Adapter<AdminOrdersAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvCode: TextView? = view.findViewById(R.id.tvAdminOrderCode)
            val tvDate: TextView? = view.findViewById(R.id.tvAdminOrderDate)
            val tvDetails: TextView? = view.findViewById(R.id.tvAdminOrderDetails)
            val tvCustom: TextView? = view.findViewById(R.id.tvAdminOrderCustomization)
            val rgStatus: RadioGroup? = view.findViewById(R.id.rgStatus)
            val rbStatus1: RadioButton? = view.findViewById(R.id.rbStatus1)
            val rbStatus2: RadioButton? = view.findViewById(R.id.rbStatus2)
            val rbStatus3: RadioButton? = view.findViewById(R.id.rbStatus3)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_order, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = orders.getOrNull(position) ?: return
            
            holder.tvCode?.text = "Pedido #${order.codigoRetiro}"
            
            val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            holder.tvDate?.text = sdf.format(Date(order.fecha))
            
            val sb = StringBuilder()
            order.items.forEach { sb.append("${it.cantidad}x ${it.nombre} (${it.talla})\n") }
            holder.tvDetails?.text = sb.toString().trim()
            
            val customizationText = if (order.customization.isEmpty()) "Ninguna" else order.customization
            holder.tvCustom?.text = "Personalización: $customizationText"
            
            val rgStatus = holder.rgStatus
            if (rgStatus != null) {
                rgStatus.setOnCheckedChangeListener(null)
                when (order.estado) {
                    1 -> holder.rbStatus1?.isChecked = true
                    2 -> holder.rbStatus2?.isChecked = true
                    3 -> holder.rbStatus3?.isChecked = true
                    else -> rgStatus.clearCheck()
                }

                rgStatus.setOnCheckedChangeListener { _, checkedId ->
                    val newStatus = when (checkedId) {
                        R.id.rbStatus1 -> 1
                        R.id.rbStatus2 -> 2
                        R.id.rbStatus3 -> 3
                        else -> order.estado
                    }
                    if (newStatus != order.estado) {
                        try {
                            FirebaseDatabase.getInstance().getReference("pedidos")
                                .child(order.codigoRetiro)
                                .child("estado")
                                .setValue(newStatus)
                        } catch (e: Exception) {
                            Toast.makeText(holder.itemView.context, "Error al actualizar estado en Firebase", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        override fun getItemCount() = orders.size
    }
}

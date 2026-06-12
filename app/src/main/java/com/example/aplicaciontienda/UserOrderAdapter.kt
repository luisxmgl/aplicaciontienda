package com.example.aplicaciontienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class UserOrderAdapter(
    private val orders: List<String>,
    private val onTrackClick: (String) -> Unit
) : RecyclerView.Adapter<UserOrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCode: TextView = view.findViewById(R.id.tvOrderCode)
        val btnTrack: MaterialButton = view.findViewById(R.id.btnGoToTracking)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val code = orders[position]
        holder.tvCode.text = "Pedido #$code"
        holder.btnTrack.setOnClickListener { onTrackClick(code) }
    }

    override fun getItemCount() = orders.size
}

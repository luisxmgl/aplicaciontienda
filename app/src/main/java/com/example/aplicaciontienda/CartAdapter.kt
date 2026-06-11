package com.example.aplicaciontienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private val items: List<CartItem>,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCartItemName)
        val tvTalla: TextView = view.findViewById(R.id.tvCartItemTalla)
        val tvPrice: TextView = view.findViewById(R.id.tvCartItemPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvCartItemQuantity)
        val btnPlus: View = view.findViewById(R.id.btnPlus)
        val btnMinus: View = view.findViewById(R.id.btnMinus)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.producto.nombre
        holder.tvTalla.visibility = View.GONE
        holder.tvPrice.text = Utils.formatPrice(item.producto.precio * item.cantidad)
        holder.tvQuantity.text = item.cantidad.toString()

        holder.btnPlus.setOnClickListener {
            CartManager.updateQuantity(item, 1)
            notifyItemChanged(position)
            onUpdate()
        }

        holder.btnMinus.setOnClickListener {
            CartManager.updateQuantity(item, -1)
            if (CartManager.getItems().contains(item)) {
                notifyItemChanged(position)
            } else {
                notifyDataSetChanged()
            }
            onUpdate()
        }

        holder.btnDelete.setOnClickListener {
            CartManager.removeItem(item)
            notifyDataSetChanged()
            onUpdate()
        }
    }

    override fun getItemCount() = items.size
}
package com.example.aplicaciontienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private var items: List<CartItem>,
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
        holder.tvTalla.text = "Talla: ${item.producto.talla}"
        holder.tvPrice.text = Utils.formatPrice(item.producto.precio * item.cantidad)
        holder.tvQuantity.text = item.cantidad.toString()

        holder.btnPlus.setOnClickListener {
            animateClick(it)
            CartManager.updateQuantity(item, 1)
            notifyItemChanged(position)
            onUpdate()
        }

        holder.btnMinus.setOnClickListener {
            animateClick(it)
            val oldQty = item.cantidad
            CartManager.updateQuantity(item, -1)
            if (oldQty > 1) {
                notifyItemChanged(position)
            } else {
                // Item was removed
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)
            }
            onUpdate()
        }

        holder.btnDelete.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                CartManager.removeItem(item)
                notifyItemRemoved(currentPos)
                notifyItemRangeChanged(currentPos, itemCount)
                onUpdate()
            }
        }
    }

    private fun animateClick(view: View) {
        view.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }
            .start()
    }

    override fun getItemCount() = items.size
}
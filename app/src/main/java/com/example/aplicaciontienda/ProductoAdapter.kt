package com.example.aplicaciontienda

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductoAdapter(
    private val productos: List<Producto>
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvTalla: TextView = view.findViewById(R.id.tvTalla)
        val tvStock: TextView = view.findViewById(R.id.tvStock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.tvNombre.text = producto.nombre
        holder.tvPrecio.text = "desde ${Utils.formatPrice(producto.precio)}"
        holder.tvTalla.text = "Talla: ${producto.talla}"
        holder.tvStock.text = "Stock: ${producto.stock}"

        holder.itemView.setOnClickListener {
            val context = it.context
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCTO", producto)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = productos.size
}

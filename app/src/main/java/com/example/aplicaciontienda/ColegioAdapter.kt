package com.example.aplicaciontienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ColegioAdapter(
    private var colegios: List<Colegio>,
    private val showCount: Boolean = false,
    private val onColegioClicked: (Colegio) -> Unit
) : RecyclerView.Adapter<ColegioAdapter.ColegioViewHolder>() {

    class ColegioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreColegio)
        val tvComuna: TextView = view.findViewById(R.id.tvComuna)
        val tvCount: TextView = view.findViewById(R.id.tvCount)
        val tvInitials: TextView = view.findViewById(R.id.tvInitials)
        val ivLogo: android.widget.ImageView = view.findViewById(R.id.ivColegioLogo)
        val card: MaterialCardView = view as MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColegioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_colegio, parent, false)
        return ColegioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColegioViewHolder, position: Int) {
        val colegio = colegios[position]
        holder.tvNombre.text = colegio.nombre
        holder.tvComuna.text = colegio.comuna
        holder.tvCount.text = colegio.productos.size.toString()
        holder.tvCount.visibility = if (showCount) View.VISIBLE else View.GONE
        
        if (colegio.logo.isNotEmpty()) {
            val resId = holder.itemView.context.resources.getIdentifier(
                colegio.logo, "drawable", holder.itemView.context.packageName
            )
            if (resId != 0) {
                holder.ivLogo.setImageResource(resId)
                holder.ivLogo.visibility = View.VISIBLE
                holder.tvInitials.visibility = View.GONE
            } else {
                holder.ivLogo.visibility = View.GONE
                holder.tvInitials.visibility = View.VISIBLE
            }
        } else {
            holder.ivLogo.visibility = View.GONE
            holder.tvInitials.visibility = View.VISIBLE
        }
        
        val initials = colegio.nombre.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it[0] }
            .joinToString("")
            .uppercase()
        holder.tvInitials.text = initials

        holder.itemView.setOnClickListener {
            onColegioClicked(colegio)
        }
    }

    override fun getItemCount() = colegios.size

    fun updateList(newList: List<Colegio>) {
        colegios = newList
        notifyDataSetChanged()
    }
}
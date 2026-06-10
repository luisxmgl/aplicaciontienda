package com.example.aplicaciontienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide

class SlideshowAdapter(private val images: List<String>) : RecyclerView.Adapter<SlideshowAdapter.SlideshowViewHolder>() {

    class SlideshowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivSlide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideshowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slideshow, parent, false)
        return SlideshowViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlideshowViewHolder, position: Int) {
        if (images.isEmpty()) return
        val realPosition = position % images.size
        val assetPath = "file:///android_asset/slide/${images[realPosition]}"
        Glide.with(holder.itemView.context)
            .load(assetPath)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = if (images.isEmpty()) 0 else Int.MAX_VALUE
}
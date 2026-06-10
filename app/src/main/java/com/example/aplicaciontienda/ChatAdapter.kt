package com.example.aplicaciontienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChatAdapter(private val messages: List<Message>, private val currentUserId: String) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessageText)
        val ivImage: ImageView = view.findViewById(R.id.ivMessageImage)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == 1) R.layout.item_message_sent else R.layout.item_message_received
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        
        if (message.text.isNotEmpty()) {
            holder.tvMessage.visibility = View.VISIBLE
            holder.tvMessage.text = message.text
        } else {
            holder.tvMessage.visibility = View.GONE
        }

        if (message.imageUrl != null) {
            holder.ivImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(message.imageUrl).into(holder.ivImage)
        } else {
            holder.ivImage.visibility = View.GONE
        }
        
        // Formato simple de hora
        holder.tvTimestamp.text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
    }

    override fun getItemCount() = messages.size
}
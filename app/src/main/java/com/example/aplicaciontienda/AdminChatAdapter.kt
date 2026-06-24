package com.example.aplicaciontienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ChatSummary(
    val chatId: String,
    val lastMessage: String,
    val hasUnread: Boolean,
    val timestamp: Long,
    val lastSeen: Long = 0L
)

class AdminChatAdapter(
    private var chats: List<ChatSummary>,
    private val onChatClicked: (String) -> Unit
) : RecyclerView.Adapter<AdminChatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvChatId)
        val tvLast: TextView = view.findViewById(R.id.tvLastMessage)
        val tvInitial: TextView = view.findViewById(R.id.tvChatInitial)
        val unreadIndicator: View = view.findViewById(R.id.unreadIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chats[position]
        holder.tvId.text = chat.chatId
        holder.tvLast.text = chat.lastMessage
        holder.unreadIndicator.visibility = if (chat.hasUnread) View.VISIBLE else View.GONE
        
        val tvPresence = holder.itemView.findViewById<TextView>(R.id.tvPresence)
        tvPresence.text = PresenceManager.getFormattedPresence(chat.lastSeen)

        holder.tvInitial.text = chat.chatId.replace("Invitado_", "").take(1).uppercase()

        holder.itemView.setOnClickListener {
            onChatClicked(chat.chatId)
        }
    }

    override fun getItemCount() = chats.size

    fun updateList(newList: List<ChatSummary>) {
        chats = newList
        notifyDataSetChanged()
    }
}
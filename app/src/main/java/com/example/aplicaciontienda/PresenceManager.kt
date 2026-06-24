package com.example.aplicaciontienda

import com.google.firebase.database.FirebaseDatabase

object PresenceManager {
    fun updateLastSeen(userId: String) {
        if (userId.isEmpty()) return
        try {
            val dbRef = FirebaseDatabase.getInstance().getReference("presence").child(userId)
            dbRef.setValue(System.currentTimeMillis())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFormattedPresence(lastSeen: Long): String {
        if (lastSeen == 0L) return "Nunca visto"
        val diff = System.currentTimeMillis() - lastSeen
        return when {
            diff < 60000 -> "En línea"
            diff < 3600000 -> "Hace ${diff / 60000} min"
            diff < 86400000 -> "Hace ${diff / 3600000} h"
            else -> "Hace ${diff / 86400000} d"
        }
    }
}

package com.example.aplicaciontienda

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

object PresenceManager {
    /**
     * Mismo shape que usa la web (`{ online, lastSeen }`) para que ambas plataformas
     * escriban/lean presencia de forma compatible en el nodo `presence/{userId}` compartido.
     */
    fun updateLastSeen(userId: String, online: Boolean = true) {
        if (userId.isEmpty()) return
        try {
            val dbRef = FirebaseDatabase.getInstance().getReference("presence").child(userId)
            dbRef.setValue(mapOf("online" to online, "lastSeen" to System.currentTimeMillis()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOffline(userId: String) {
        if (userId.isEmpty()) return
        try {
            FirebaseDatabase.getInstance().getReference("presence").child(userId)
                .setValue(mapOf("online" to false, "lastSeen" to System.currentTimeMillis()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Lee `lastSeen` tolerando datos viejos guardados como Long plano (formato anterior de
     * Android) y el shape de objeto `{online, lastSeen}` que ya escribe la web, sin lanzar
     * excepción ante ninguno de los dos (evita el crash de CustomClassMapper).
     */
    fun extractLastSeen(snapshot: DataSnapshot): Long {
        return try {
            snapshot.getValue(Long::class.java) ?: 0L
        } catch (e: Exception) {
            try {
                snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
            } catch (e2: Exception) {
                0L
            }
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

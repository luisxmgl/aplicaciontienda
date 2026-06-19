package com.example.aplicaciontienda

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val imageUrl: String? = null,
    val cartSummary: String? = null,
    val read: Boolean = false
)
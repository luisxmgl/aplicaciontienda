package com.example.aplicaciontienda

import android.content.Context
import android.content.SharedPreferences

object PointsManager {
    private const val PREFS_NAME = "points_prefs"
    private const val KEY_POINTS = "user_points"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getPoints(context: Context): Int {
        return getPrefs(context).getInt(KEY_POINTS, 0)
    }

    fun addPoints(context: Context, amount: Int) {
        val current = getPoints(context)
        getPrefs(context).edit().putInt(KEY_POINTS, current + amount).apply()
    }

    fun redeemPoints(context: Context, amount: Int): Boolean {
        val current = getPoints(context)
        if (current >= amount) {
            getPrefs(context).edit().putInt(KEY_POINTS, current - amount).apply()
            return true
        }
        return false
    }

    // Canje personalizado por producto
    fun calculatePoints(total: Int): Int {
        return total / 5000
    }

    fun calculateTotalPoints(items: List<CartItem>): Int {
        return items.sumOf { it.producto.puntosEarn * it.cantidad }
    }
}
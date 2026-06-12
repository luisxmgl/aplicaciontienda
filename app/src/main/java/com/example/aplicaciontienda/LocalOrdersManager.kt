package com.example.aplicaciontienda

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LocalOrdersManager {
    private const val PREFS_NAME = "LocalOrdersPrefs"
    private const val KEY_ORDERS = "orders"

    fun saveOrder(context: Context, code: String) {
        val orders = getOrders(context).toMutableList()
        if (!orders.contains(code)) {
            orders.add(0, code)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_ORDERS, Gson().toJson(orders)).apply()
        }
    }

    fun getOrders(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ORDERS, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type)
    }
}

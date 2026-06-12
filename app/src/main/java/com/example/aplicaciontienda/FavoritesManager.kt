package com.example.aplicaciontienda

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FavoritesManager {
    private const val PREFS_NAME = "favorites_prefs"
    private const val KEY_FAVORITES = "favorites_list"
    private val favorites = mutableListOf<Producto>()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_FAVORITES, null)
        if (json != null) {
            val type = object : TypeToken<List<Producto>>() {}.type
            val list: List<Producto> = Gson().fromJson(json, type)
            favorites.clear()
            favorites.addAll(list)
        }
    }

    private fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(favorites)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }

    fun toggleFavorite(context: Context, producto: Producto) {
        if (isFavorite(producto)) {
            favorites.removeAll { it.nombre == producto.nombre && it.colegio == producto.colegio }
        } else {
            favorites.add(producto)
        }
        save(context)
    }

    fun isFavorite(producto: Producto): Boolean {
        return favorites.any { it.nombre == producto.nombre && it.colegio == producto.colegio }
    }

    fun getFavorites(): List<Producto> = favorites
}
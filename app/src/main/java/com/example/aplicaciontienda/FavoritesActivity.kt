package com.example.aplicaciontienda

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarFavorites)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rvFavorites = findViewById<RecyclerView>(R.id.rvFavorites)
        rvFavorites.layoutManager = GridLayoutManager(this, 2)
        
        val favoritesList = FavoritesManager.getFavorites()
        val adapter = ProductoAdapter(favoritesList)
        rvFavorites.adapter = adapter

        if (favoritesList.isEmpty()) {
            findViewById<TextView>(R.id.tvEmptyFavorites).visibility = View.VISIBLE
            rvFavorites.visibility = View.GONE
        }
    }
}
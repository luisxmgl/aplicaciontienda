package com.example.aplicaciontienda

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyOrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_my_orders)
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
            return
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarMyOrders)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener { finish() }
        }

        val rvOrders = findViewById<RecyclerView>(R.id.rvMyOrders)
        val tvNoOrders = findViewById<TextView>(R.id.tvNoOrders)

        if (rvOrders != null) {
            rvOrders.layoutManager = LinearLayoutManager(this)
            
            val orders = try {
                LocalOrdersManager.getOrders(this)
            } catch (e: Exception) {
                emptyList<String>()
            }
            
            if (orders.isEmpty()) {
                tvNoOrders?.visibility = View.VISIBLE
                rvOrders.visibility = View.GONE
            } else {
                tvNoOrders?.visibility = View.GONE
                rvOrders.visibility = View.VISIBLE
                rvOrders.adapter = UserOrderAdapter(orders) { code ->
                    if (!code.isNullOrEmpty()) {
                        val intent = Intent(this, TrackingActivity::class.java)
                        intent.putExtra("ORDER_CODE", code)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

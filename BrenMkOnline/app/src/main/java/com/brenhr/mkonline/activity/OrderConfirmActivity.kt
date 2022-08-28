package com.brenhr.mkonline.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import com.brenhr.mkonline.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class OrderConfirmActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var orderId: String
    private lateinit var totalItems: String
    private lateinit var totalCost: String
    private lateinit var user: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirm)

        auth = Firebase.auth
        user = auth.currentUser!!

        orderId = intent.getStringExtra("orderId")!!
        totalItems = intent.getStringExtra("items")!!
        totalCost = intent.getStringExtra("total")!!

        setup()
    }

    private fun setup() {
        val order = findViewById<TextView>(R.id.orderId)
        val items = findViewById<TextView>(R.id.items)
        val total = findViewById<TextView>(R.id.totalText)
        val textEmail = findViewById<TextView>(R.id.textEmail)
        order.text = orderId
        items.text = totalItems
        total.text = totalCost
        textEmail.text = "We will send you updates to your mail: $textEmail"
    }
}
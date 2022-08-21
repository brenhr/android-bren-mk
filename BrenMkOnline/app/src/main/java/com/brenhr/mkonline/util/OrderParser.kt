package com.brenhr.mkonline.util

import com.brenhr.mkonline.model.Order
import com.google.firebase.firestore.DocumentSnapshot

class OrderParser {

    fun parse(document: DocumentSnapshot): Order {
        val id = document.id
        val status = document.data?.get("status") as String
        val quantity = document.data?.get("quantity") as Long
        val total = document.data?.get("total") as Double

        return Order(id, status, quantity, total)
    }
}
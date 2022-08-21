package com.brenhr.mkonline.util

import com.brenhr.mkonline.model.Item
import com.google.firebase.firestore.DocumentSnapshot

class ItemParser {

    fun parse(document: DocumentSnapshot): Item {
        val id = document.id
        val productId = document.data?.get("productId") as String
        val quantity = document.data?.get("quantity") as Long
        val color = document.data?.get("color") as String
        val size = document.data?.get("size") as String

        return Item(id, productId, quantity, color, size)
    }
}
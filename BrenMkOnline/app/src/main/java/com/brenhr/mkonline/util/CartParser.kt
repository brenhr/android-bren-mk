package com.brenhr.mkonline.util

import com.brenhr.mkonline.model.Cart
import com.brenhr.mkonline.model.ItemCart
import com.brenhr.mkonline.model.Product
import com.google.firebase.firestore.DocumentSnapshot

class CartParser {

    fun parseOrder(order: DocumentSnapshot): Cart {
        val id = order.id
        val quantity = order.data?.get("quantity") as Long
        val status = order.data?.get("status") as String
        val total = order.data?.get("total") as Double

        return Cart(id, status, quantity, total)
    }

    fun parseItem(item: DocumentSnapshot, product: Product, imageUrl: String): ItemCart {
        val id = item.id
        val productId = item.data?.get("productId") as String
        val quantity = item.data?.get("quantity") as Long
        val color = item.data?.get("color") as String
        val size = item.data?.get("size") as String

        return ItemCart(productId, quantity, color, size, product, imageUrl)
    }
}
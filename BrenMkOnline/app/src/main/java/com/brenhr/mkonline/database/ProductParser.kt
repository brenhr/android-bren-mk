package com.brenhr.mkonline.database

import android.util.Log
import com.brenhr.mkonline.model.Product
import com.brenhr.mkonline.model.ProductDetail
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProductParser {

    fun byId(id: String) {
        val database = Firebase.database
        val myRef = database.getReference("Product")
        myRef.child(id).get().addOnSuccessListener {
            Log.i("database", "Got value ${it.value}")

        }.addOnFailureListener{
            Log.e("database", "Error getting data", it)
        }

    }

    fun parser(productSnapshot: DataSnapshot): Product {
        val id = productSnapshot.key.toString()
        val brandId = productSnapshot.child("brandId").value.toString()
        val categoryId = productSnapshot.child("categoryId").value.toString()
        val currency = productSnapshot.child("currency").value.toString()
        val datetime = productSnapshot.child("datetime").value.toString()
        val description = productSnapshot.child("description").value.toString()

        val details = mutableListOf<ProductDetail>()
        productSnapshot.child("details").children.forEach {
            details.add(detailParser(it))
        }

        val name = productSnapshot.child("name").value.toString()
        val price = productSnapshot.child("price").value.toString().toDouble()
        val sku = productSnapshot.child("sku").value.toString()

        return Product(id, brandId, categoryId, currency, datetime, description, details, name, price, sku)

    }

    private fun detailParser(detailSnapshot: DataSnapshot): ProductDetail {
        val id = detailSnapshot.key.toString()
        val color = detailSnapshot.child("color").value.toString()
        val size = detailSnapshot.child("size").value.toString()
        val sold = detailSnapshot.child("sold").value.toString().toInt()
        val stock = detailSnapshot.child("stock").value.toString().toInt()

        return ProductDetail(id, color, size, sold, stock)
    }
}
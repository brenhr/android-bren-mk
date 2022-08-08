package com.brenhr.mkonline.database

import android.util.Log
import com.brenhr.mkonline.model.Product
import com.brenhr.mkonline.model.ProductDetail
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProductService {

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
        Log.i("ProductParser", "Parsing product: "+ productSnapshot.key)
        val id = productSnapshot.key.toString()
        Log.i("ProductParser", "ID: $id")
        val brandId = productSnapshot.child("brandId").value.toString()
        Log.i("ProductParser", "brandId: $brandId")
        val categoryId = productSnapshot.child("categoryId").value.toString()
        Log.i("ProductParser", "categoryId: $categoryId")
        val currency = productSnapshot.child("currency").value.toString()
        Log.i("ProductParser", "currency: $currency")
        val datetime = productSnapshot.child("datetime").value.toString()
        Log.i("ProductParser", "datetime: $datetime")
        val description = productSnapshot.child("description").value.toString()
        Log.i("ProductParser", "description: $description")

        val details = mutableListOf<ProductDetail>()
        productSnapshot.child("details").children.forEach {
            details.add(detailParser(it))
            Log.i("database", "Detail added")
        }

        val name = productSnapshot.child("name").value.toString()
        Log.i("ProductParser", "name: $name")
        val price = productSnapshot.child("price").value.toString().toDouble()
        Log.i("ProductParser", "price: $price")
        val sku = productSnapshot.child("sku").value.toString()
        Log.i("ProductParser", "sku: $sku")

        return Product(id, brandId, categoryId, currency, datetime, description, details, name, price, sku)

    }

    private fun detailParser(detailSnapshot: DataSnapshot): ProductDetail {
        Log.i("DetailParser", "Parsing detail ID : ${detailSnapshot.key}")
        val id = detailSnapshot.key.toString()
        Log.i("DetailParser", "id : $id")
        val color = detailSnapshot.child("color").value.toString()
        Log.i("DetailParser", "color : $color")
        val size = detailSnapshot.child("size").value.toString()
        Log.i("DetailParser", "size : $size")
        val sold = detailSnapshot.child("sold").value.toString().toInt()
        Log.i("DetailParser", "sold : $sold")
        val stock = detailSnapshot.child("stock").value.toString().toInt()
        Log.i("DetailParser", "stock : $stock")

        return ProductDetail(id, color, size, sold, stock)
    }
}
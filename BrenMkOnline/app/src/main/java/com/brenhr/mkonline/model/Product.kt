package com.brenhr.mkonline.model

import android.telecom.Call
import java.time.chrono.ChronoLocalDateTime

class Product {
    var id: String? = null
    var brandId: String? = null
    var categoryId: String? = null
    var currency: String? = null
    var datetime: String? = null
    var description: String? = null
    var details: MutableList<ProductDetail>? = null
    var name: String? = null
    var price: Double? = null
    var sku: String? = null

    constructor(id: String, brandId: String, categoryId: String, currency: String, datetime: String,
                description: String, details: MutableList<ProductDetail>, name: String, price: Double,
                sku: String) {
        this.id = id
        this.brandId = brandId
        this.categoryId = categoryId
        this.currency = currency
        this.datetime = datetime
        this.description = description
        this.details = details
        this.name = name
        this.price = price
        this.sku = sku
    }
}
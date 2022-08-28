package com.brenhr.mkonline.model

import android.telecom.Call
import java.time.chrono.ChronoLocalDateTime

class Product {
    var id: String
    var brandId: String
    var categoryId: String
    var currency: String
    var datetime: String
    var description: String
    var details: MutableList<ProductDetail>
    var name: String
    var price: Long
    var sku: String

    constructor(id: String, brandId: String, categoryId: String, currency: String, datetime: String,
                description: String, details: MutableList<ProductDetail>, name: String, price: Long,
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
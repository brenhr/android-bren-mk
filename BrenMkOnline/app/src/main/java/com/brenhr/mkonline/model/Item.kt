package com.brenhr.mkonline.model

class Item {
    var id: String?
    var productId: String
    var quantity: Long
    var color: String
    var size: String

    constructor(productId: String, quantity: Long, color: String, size: String) {
        this.id = null
        this.color = color
        this.productId = productId
        this.quantity = quantity
        this.size = size
    }

    constructor(id: String, productId: String, quantity: Long, color: String, size: String) {
        this.id = id
        this.color = color
        this.productId = productId
        this.quantity = quantity
        this.size = size
    }

}
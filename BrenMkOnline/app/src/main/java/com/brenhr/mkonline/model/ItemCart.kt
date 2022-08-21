package com.brenhr.mkonline.model

class ItemCart {
    var productId: String
    var quantity: Long
    var color: String
    var size: String
    var product: Product
    var imageUrl: String

    constructor(productId: String, quantity: Long, color: String, size: String, product: Product,
                imageUrl: String) {
        this.productId = productId
        this.quantity = quantity
        this.color = color
        this.size = size
        this.product = product
        this.imageUrl = imageUrl
    }
}
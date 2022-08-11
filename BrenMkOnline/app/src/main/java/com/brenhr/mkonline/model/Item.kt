package com.brenhr.mkonline.model

class Item {
    var color: String
    var itemId: String
    var productId: String
    var quantity: Int
    var size: String

    constructor(color: String, itemId: String, productId: String, quantity: Int, size: String) {
        this.color = color
        this.itemId = itemId
        this.productId = productId
        this.quantity = quantity
        this.size = size
    }

}
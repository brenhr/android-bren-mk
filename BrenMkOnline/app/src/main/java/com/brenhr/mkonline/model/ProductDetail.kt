package com.brenhr.mkonline.model

class ProductDetail {
    var id: String
    var color: String
    var size: String
    var sold: Int
    var stock: Int

    constructor(id: String, color: String, size: String, sold: Int, stock: Int) {
        this.id = id
        this.color = color
        this.size = size
        this.sold = sold
        this.stock = stock
    }
}
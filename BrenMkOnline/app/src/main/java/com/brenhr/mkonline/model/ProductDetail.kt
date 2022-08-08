package com.brenhr.mkonline.model

class ProductDetail {
    private var id: String? = null
    private var color: String? = null
    private var size: String? = null
    private var sold: Int? = null
    private var stock: Int? = null

    constructor(id: String, color: String, size: String, sold: Int, stock: Int) {
        this.id = id
        this.color = color
        this.size = size
        this.sold = sold
        this.stock = stock
    }
}
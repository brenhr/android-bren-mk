package com.brenhr.mkonline.model

class Cart {
    var id: String
    var status: String
    var items: MutableList<ItemCart>?
    var quantity: Long
    var total: Long

    constructor(id: String,status: String, quantity: Long, total: Long) {
        this.id = id
        this.status = status
        this.items = null
        this.quantity = quantity
        this.total = total
    }
}
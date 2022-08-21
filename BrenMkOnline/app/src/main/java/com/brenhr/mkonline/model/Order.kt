package com.brenhr.mkonline.model

class Order {
    var id: String?
    var status: String
    var items: MutableList<Item>?
    var quantity: Long
    var total: Double

    constructor(id: String, status: String, items: MutableList<Item>, quantity: Long, total: Double) {
        this.id = id
        this.status = status
        this.items = items
        this.quantity = quantity
        this.total = total
    }

    constructor(id: String, status: String, quantity: Long, total: Double) {
        this.id = id
        this.status = status
        this.items = null
        this.quantity = quantity
        this.total = total
    }

    constructor(status: String, quantity: Long, total: Double) {
        this.id = null
        this.status = status
        this.items = null
        this.quantity = quantity
        this.total = total
    }
}
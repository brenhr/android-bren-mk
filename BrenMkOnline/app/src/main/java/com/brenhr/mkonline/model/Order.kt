package com.brenhr.mkonline.model

class Order {
    var completed: Boolean
    var date: String
    var items: MutableList<Item>
    var orderId: String
    var quantity: Int
    var total: Double

    constructor(completed: Boolean, date: String, items: MutableList<Item>, orderId: String,
                quantity: Int, total: Double) {
        this.completed = completed
        this.date = date
        this.items = items
        this.orderId = orderId
        this.quantity = quantity
        this.total = total
    }
}
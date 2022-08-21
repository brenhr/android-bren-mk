package com.brenhr.mkonline.model

class User {
    var uid: String?
    var name: String
    var lastName: String
    var email: String
    var profilePicture: String
    var orders: MutableList<Order>?
    var reviews: MutableList<Review>?
    var address: Address?
    var cart: Order?


    constructor(uid: String, name: String, lastName: String, email: String, profilePicture: String,
                orders: MutableList<Order>, reviews: MutableList<Review>, address: Address) {
        this.uid = uid
        this.name = name
        this.lastName = lastName
        this.email = email
        this.profilePicture= profilePicture
        this.address = address
        this.orders = orders
        this.reviews = reviews
        this.cart = null
    }

    constructor(name: String, lastName: String, email: String, profilePicture: String) {
        this.name = name
        this.lastName = lastName
        this.email = email
        this.profilePicture= profilePicture
        this.address = null
        this.orders = null
        this.reviews = null
        this.uid = null
        this.cart = null
    }

    constructor(uid: String, name: String, lastName: String, email: String, profilePicture: String) {
        this.uid = uid
        this.name = name
        this.lastName = lastName
        this.email = email
        this.profilePicture= profilePicture
        this.address = null
        this.orders = null
        this.reviews = null
        this.cart = null
    }
}
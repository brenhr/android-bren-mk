package com.brenhr.mkonline.model

class User {
    var name: String
    var lastName: String
    var email: String
    var profilePicture: String
    var orders: MutableList<Order>?
    var address: Address?


    constructor(name: String, lastName: String, email: String, profilePicture: String,
                orders: MutableList<Order>, reviews: MutableList<Review>, address: Address) {
        this.name = name
        this.lastName = lastName
        this.email = email
        this.profilePicture= profilePicture
        this.address = address
        this.orders = orders
    }

    constructor(name: String, lastName: String, email: String, profilePicture: String) {
        this.name = name
        this.lastName = lastName
        this.email = email
        this.profilePicture= profilePicture
        this.address = null
        this.orders = null
    }
}
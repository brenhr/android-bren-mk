package com.brenhr.mkonline.model

class Address {
    var street: String
    var number: String
    var city: String
    var zipCode: String
    var phoneNumber: String

    constructor(street: String, number: String, city: String, zipCode: String,
                phoneNumber: String) {
        this.street = street
        this.number = number
        this.city = city
        this.zipCode = zipCode
        this.phoneNumber = phoneNumber
    }
}
package com.brenhr.mkonline.model

class Address {
    var street: String
    var number: String
    var neighborhood: String
    var city: String
    var zipCode: String
    var phoneNumber: String

    constructor(street: String, number: String, neighborhood: String, city: String, zipCode: String,
                phoneNumber: String) {
        this.street = street
        this.number = number
        this.neighborhood = neighborhood
        this.city = city
        this.zipCode = zipCode
        this.phoneNumber = phoneNumber
    }
}
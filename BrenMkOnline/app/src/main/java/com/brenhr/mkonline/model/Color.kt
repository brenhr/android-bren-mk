package com.brenhr.mkonline.model

class Color {
    var id: String
    var hexcode: String
    var name: String

    constructor(id: String, hexcode: String, name: String) {
        this.id = id
        this.hexcode = hexcode
        this.name = name
    }
}
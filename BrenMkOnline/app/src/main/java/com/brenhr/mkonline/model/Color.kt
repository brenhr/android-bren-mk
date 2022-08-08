package com.brenhr.mkonline.model

class Color {
    private var id: String? = null
    private var hexcode: String? = null
    private var name: String? = null

    constructor(id: String, hexcode: String, name: String) {
        this.id = id
        this.hexcode = hexcode
        this.name = name
    }
}
package com.brenhr.mkonline.model

class Review {
    var id: String
    var productId: String
    var userId: String
    var rate: String
    var comment: String?

    constructor(id: String, productId: String, userId: String, rate: String, comment: String) {
        this.id = id
        this.productId = productId
        this.userId = userId
        this.rate = rate
        this.comment = comment
    }
}
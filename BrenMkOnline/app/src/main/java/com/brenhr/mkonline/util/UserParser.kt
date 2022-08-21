package com.brenhr.mkonline.util

import com.brenhr.mkonline.model.User
import com.google.firebase.firestore.DocumentSnapshot

class UserParser {

    fun parser(document: DocumentSnapshot): User {
        val uid = document.id
        val name = document.data?.get("name") as String
        val lastName = document.data?.get("lastName") as String
        val email = document.data?.get("email") as String
        val profilePicture = document.data?.get("name") as String

        return User(uid, name, lastName, email, profilePicture)
    }
}
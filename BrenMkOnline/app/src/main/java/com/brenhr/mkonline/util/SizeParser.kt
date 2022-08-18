package com.brenhr.mkonline.util

import com.brenhr.mkonline.model.Size
import com.google.firebase.database.DataSnapshot

class SizeParser {

    fun parser(sizeSnapshot: DataSnapshot): Size {
        val id = sizeSnapshot.key.toString()
        val name = sizeSnapshot.child("name").value.toString()

        return Size(id,name)
    }
}
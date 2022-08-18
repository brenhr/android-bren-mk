package com.brenhr.mkonline.util

import com.brenhr.mkonline.model.Color
import com.google.firebase.database.DataSnapshot

class ColorParser {

    fun parser(colorSnapshot: DataSnapshot): Color {
        val id = colorSnapshot.key.toString()
        val name = colorSnapshot.child("name").value.toString()
        val hexcode = colorSnapshot.child("name").value.toString()

        return Color(id, hexcode, name)
    }
}
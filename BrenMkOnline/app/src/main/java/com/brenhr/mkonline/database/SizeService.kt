package com.brenhr.mkonline.database

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SizeService {
    private val database = Firebase.database
    private val myRef = database.getReference("Size")

    fun getAll() {
        myRef.get().addOnSuccessListener {
            it.children.forEach { child ->
                Log.i("database", "Got value ${child.value}")
            }
        }.addOnFailureListener{
            Log.e("database", "Error getting data", it)
        }
    }

    fun byId(id: String) {
        myRef.child(id).get().addOnSuccessListener {
            Log.i("database", "Got value ${it.value}")
        }.addOnFailureListener{
            Log.e("database", "Error getting data", it)
        }
    }
}
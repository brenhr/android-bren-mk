package com.brenhr.mkonline.util

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.stripe.android.EphemeralKeyProvider
import com.stripe.android.EphemeralKeyUpdateListener

class FirebaseEphemeralKeyProvider: EphemeralKeyProvider {
    private var functions: FirebaseFunctions = Firebase.functions
    override fun createEphemeralKey(
        apiVersion: String,
        keyUpdateListener: EphemeralKeyUpdateListener
    ) {
        val data = hashMapOf("apiVersion" to apiVersion)
        functions.getHttpsCallable("createEphemeralKey")
            .call(data)
            .continueWith { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    if (e is FirebaseFunctionsException) {
                        val code = e.code
                        val message = e.message
                        Log.e("EphemeralKey", "Error when creating ephemeral key: $code: $message")
                    }
                }
                val key = task.result?.data.toString()
                Log.d("EphemeralKey", "Ephemeral key created: $key")
                keyUpdateListener.onKeyUpdate(key)
            }
    }
}
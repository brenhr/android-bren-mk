package com.brenhr.mkonline.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.brenhr.mkonline.R
import com.brenhr.mkonline.model.Address
import com.brenhr.mkonline.model.Cart
import com.brenhr.mkonline.util.CartParser
import com.brenhr.mkonline.util.FirebaseEphemeralKeyProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.stripe.android.*
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.view.BillingAddressFields

class CheckoutActivity : AppCompatActivity() {

    private lateinit var orderId: String

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var user: FirebaseUser

    private lateinit var cart: Cart
    private lateinit var cartParser: CartParser

    private lateinit var paymentSession: PaymentSession

    private lateinit var selectedPaymentMethod: PaymentMethod

    private val stripe: Stripe by lazy {
        Stripe(applicationContext,
            PaymentConfiguration.getInstance(applicationContext).publishableKey)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        PaymentConfiguration.init(this,
            "pk_test_51Kfz2GBrA4HBKbc77BX6OHYlMmKNn265NfKaZuWqjMbx1zQPS5b4n6eUa3RxZhHSEbOc5sNXMUwsTiF7sszBYTtf00Hpx8crY2");
        orderId = intent.getStringExtra("orderId")!!

        val paymentButton = findViewById<Button>(R.id.paymentButton)
        paymentButton.isEnabled = true

        auth = Firebase.auth
        firestore = Firebase.firestore
        user = auth.currentUser!!
        cartParser = CartParser()
        fillCheckoutFields()
        showCheckoutDetails()
        setupPaymentSession()
    }

    private fun showCheckoutDetails() {
        val orderReference = firestore.collection("users").document(user.uid)
            .collection("orders").document(orderId)
        orderReference.get()
            .addOnSuccessListener { document ->
                if(document.exists()) {
                    Log.d("ProductItems", "Items: ${document.id} => ${document.data}")
                    cart = cartParser.parseOrder(document)
                    val totalItems = findViewById<TextView>(R.id.totalItems)
                    totalItems.text = "Products in cart: ${cart.quantity}"
                    val totalOrder = findViewById<TextView>(R.id.totalCost)
                    totalOrder.text = "Total: $ ${cart.total} USD"

                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreItems", "Error getting cart items: ", exception)
            }

        val paymentButton = findViewById<Button>(R.id.paymentButton)

        paymentButton.setOnClickListener {
            val paymentMethod = findViewById<TextView>(R.id.paymentMethod)
            if(paymentButton.text.toString() == "Confirm data and pay") {
                createFirestorePayment()
            }  else {
                paymentSession.presentPaymentMethodSelection()
                if(paymentMethod.text.toString() != "No payment selected") {
                    paymentButton.text = "Confirm data and pay"
                }
            }
        }
    }

    private fun createFirestorePayment() {
        val name = findViewById<EditText>(R.id.nameField).text.toString()
        val lastName = findViewById<EditText>(R.id.lastNameField).text.toString()
        val email = findViewById<EditText>(R.id.emailField).text.toString()
        val street = findViewById<EditText>(R.id.streetField).text.toString()
        val number = findViewById<EditText>(R.id.numberField).text.toString()
        val city = findViewById<EditText>(R.id.cityField).text.toString()
        val zipCode = findViewById<EditText>(R.id.zipCodeField).text.toString()
        val state = findViewById<EditText>(R.id.stateField).text.toString()
        val phoneNumber = findViewById<EditText>(R.id.phoneField).text.toString()

        if(name.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() &&
            street.isNotEmpty() && number.isNotEmpty() && city.isNotEmpty()
            && zipCode.isNotEmpty() && state.isNotEmpty() && phoneNumber.isNotEmpty()) {
                val address = Address(street, number, city, zipCode, phoneNumber)
                showProgress()
                saveAddress(address)
        } else {
            hideProgress()
            Toast.makeText(this, "Please fill out all the fields.",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAddress(address: Address) {
        firestore.collection("users")
            .document(user.uid).update("address", address)
            .addOnSuccessListener {
                Log.d("FirestorePayment", "Address object was added!")
                processPayment()
            }.addOnFailureListener { e ->
                Log.w("FirestorePayment", "Error updating address", e)
                hideProgress()
                Toast.makeText(this, "The payment couldn't be processed. Please try later",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun processPayment() {
        val paymentButton = findViewById<Button>(R.id.paymentButton)
        paymentButton.isEnabled = false

        val paymentCollection = Firebase.firestore
            .collection("stripe_customers").document(user?.uid?:"")
            .collection("payments")
        val finalAmount = cart.total * 100
        paymentCollection.add(hashMapOf(
            "amount" to finalAmount,
            "currency" to "usd"
        ))
            .addOnSuccessListener { documentReference ->
                Log.d("payment", "DocumentSnapshot added with ID: ${documentReference.id}")
                documentReference.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("payment", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        if(snapshot.data?.get("status").toString() == "succeeded") {
                            updateOrderStatus(orderId)
                        }
                        Log.d("payment", "Current data: ${snapshot.data}")
                        val clientSecret = snapshot.data?.get("client_secret")
                        Log.d("payment", "Create paymentIntent returns $clientSecret")
                        clientSecret?.let {
                            stripe.confirmPayment(this, ConfirmPaymentIntentParams.createWithPaymentMethodId(
                                selectedPaymentMethod.id!!,
                                (it as String)
                            ))
                        }
                    } else {
                        Log.e("payment", "Current payment intent : null")
                        Toast.makeText(this, "Payment couldn't be processed. Please try again.",
                            Toast.LENGTH_SHORT).show()
                        hideProgress()
                        paymentButton.isEnabled = true
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("payment", "Error adding document", e)
                hideProgress()
                Toast.makeText(this, "Payment couldn't be processed.",
                    Toast.LENGTH_SHORT).show()
                paymentButton.isEnabled = true
            }
    }

    private fun updateOrderStatus(orderId: String) {
        firestore.collection("users").document(user.uid)
            .collection("orders").document(orderId)
            .update("status", "confirmed").addOnSuccessListener {
                showOrderConfirmation(cart)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error when confirming payment.",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun fillCheckoutFields() {
        var userReference = firestore.collection("users").document(user.uid)
        userReference.get().addOnSuccessListener { document ->
            if(document.exists()) {
                val name = document.data?.get("name") as String
                val lastName = document.data?.get("lastName") as String
                val email = document.data?.get("email") as String

                if(name.isNotEmpty() && lastName.isNotEmpty()) {
                    val nameField = findViewById<EditText>(R.id.nameField)
                    val lastNameField = findViewById<EditText>(R.id.lastNameField)
                    val emailField = findViewById<EditText>(R.id.emailField)

                    nameField.setText(name)
                    lastNameField.setText(lastName)
                    emailField.setText(email)
                    emailField.isEnabled = false;
                }
            }
        }
    }

    private fun setupPaymentSession() {
        CustomerSession.initCustomerSession(this, FirebaseEphemeralKeyProvider())
        val paymentButton = findViewById<Button>(R.id.paymentButton)

        paymentSession = PaymentSession(this, PaymentSessionConfig.Builder()
            .setShippingInfoRequired(false)
            .setShippingMethodsRequired(false)
            .setBillingAddressFields(BillingAddressFields.None)
            .setShouldShowGooglePay(false)
            .build())

        paymentSession.init(
            object: PaymentSession.PaymentSessionListener {
                override fun onPaymentSessionDataChanged(data: PaymentSessionData) {
                    Log.d("PaymentSession", "PaymentSession has changed: $data")
                    Log.d("PaymentSession", "${data.isPaymentReadyToCharge} <> ${data.paymentMethod}")

                    if (data.isPaymentReadyToCharge) {
                        Log.d("PaymentSession", "Ready to charge");
                        paymentButton.isEnabled = true

                        data.paymentMethod?.let {
                            Log.d("PaymentSession", "PaymentMethod $it selected")
                            val paymentMethod = findViewById<TextView>(R.id.paymentMethod)
                            paymentMethod.text = "${it.card?.brand} card ends with ${it.card?.last4}"
                            paymentButton.text = "Confirm data and pay"
                            selectedPaymentMethod = it
                        }
                    }
                }

                override fun onCommunicatingStateChanged(isCommunicating: Boolean) {
                    Log.d("PaymentSession",  "isCommunicating $isCommunicating")
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    Log.e("PaymentSession",  "onError: $errorCode, $errorMessage")
                }
            }
        )
    }

    private fun showOrderConfirmation(cart: Cart) {
        val intent = Intent (this, OrderConfirmActivity::class.java)
        intent.putExtra("orderId", cart.id)
        intent.putExtra("items", cart.quantity)
        intent.putExtra("total", cart.total)
        this.startActivity(intent)
    }

    private fun showProgress() {
        val progressBar: RelativeLayout = findViewById(R.id.loadingPanel)
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        val progressBar: RelativeLayout = findViewById(R.id.loadingPanel)
        progressBar.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            paymentSession.handlePaymentData(requestCode, resultCode, data)
        }
    }

}
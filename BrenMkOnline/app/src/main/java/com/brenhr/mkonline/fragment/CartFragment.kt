package com.brenhr.mkonline.fragment

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.brenhr.mkonline.R
import com.brenhr.mkonline.activity.CheckoutActivity
import com.brenhr.mkonline.model.Cart
import com.brenhr.mkonline.model.ItemCart
import com.brenhr.mkonline.model.Product
import com.brenhr.mkonline.util.CartParser
import com.brenhr.mkonline.util.ProductParser
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class CartFragment : Fragment() {

    private lateinit var orderId: String

    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private lateinit var user: FirebaseUser
    private lateinit var productParser: ProductParser
    private lateinit var cartParser: CartParser

    private lateinit var cart: Cart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        firestore = Firebase.firestore
        database = Firebase.database
        storage = Firebase.storage

        user = auth.currentUser!!

        productParser = ProductParser()
        cartParser = CartParser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(arguments != null) {
            orderId = arguments?.getString("orderId")!!
            showCartView()
            getOrder()
        } else {
            findOrderByUserId()
        }

        val checkoutButton: Button = this.requireView().findViewById(R.id.checkoutButton)
        checkoutButton.setOnClickListener {
            showCheckoutView(orderId)
        }
    }

    private fun findOrderByUserId() {
        val reference = firestore.collection("users").document(user!!.uid)
            .collection("orders")
        reference.whereEqualTo("status", "cart").limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if(!documents.isEmpty) {
                    showCartView()
                    for (document in documents) {
                        Log.d("FirestoreOrder", "Cart: ${document.id} => ${document.data}")
                        orderId = document.id
                        getOrder()
                    }
                } else {
                    showEmptyCartView()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting user cart: ", exception)
            }
    }

    private fun getOrder() {
        val orderReference = firestore.collection("users").document(user.uid)
            .collection("orders").document(orderId)
        orderReference.get()
            .addOnSuccessListener { document ->
                if(document.exists()) {
                    Log.d("ProductItems", "Items: ${document.id} => ${document.data}")
                    cart = cartParser.parseOrder(document)
                    val totalItems = this.requireView().findViewById<TextView>(R.id.productsInCart)
                    totalItems.text = "Products in cart: ${cart.quantity}"
                    val totalOrder = this.requireView().findViewById<TextView>(R.id.totalOrder)
                    totalOrder.text = "Total: $ ${cart.total} USD"

                    val totalOrderBottom = this.requireView().findViewById<TextView>(R.id.totalOrderBottom)
                    totalOrderBottom.text = "Total: $ ${cart.total} USD"
                    getItems()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreItems", "Error getting cart items: ", exception)
            }
    }

    private fun getItems() {
        val itemReference = firestore.collection("users").document(user.uid)
            .collection("orders").document(orderId).collection("items")
        itemReference.get()
            .addOnSuccessListener { documents ->
                if(!documents.isEmpty) {
                    for (document in documents) {
                        Log.d("ProductItems", "Items: ${document.id} => ${document.data}")
                        getProduct(document)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreItems", "Error getting cart items: ", exception)
            }

    }

    private fun getProduct(document: DocumentSnapshot) {
        val productId = document.data?.get("productId") as String

        val myRef = database.getReference("Product")
        myRef.child(productId).get().addOnSuccessListener {
            Log.i("database", "Got value ${it.value}")
            val product = productParser.parser(it)
            getImageFromStorage(product, document)
        }.addOnFailureListener{
            Log.e("database", "Error getting data", it)
        }
    }

    private fun getImageFromStorage(product: Product, document: DocumentSnapshot) {
        val storageRef = storage.reference
        val sku = product.sku.lowercase()
        val spaceRef = storageRef.child("clothes/$sku/$sku-main.png")
        Log.i("storage","URI: ${spaceRef.path}")
        spaceRef.downloadUrl.addOnSuccessListener {
            val itemCart = cartParser.parseItem(document, product, it.toString())
            populateCart(itemCart)
        }.addOnFailureListener {
            Log.e("storage", it.toString())
        }
    }

    private fun populateCart(itemCart: ItemCart) {
        Log.d("CartItems", "Populate items: ${itemCart.imageUrl}")
        var row = TableRow(this.requireContext())

        //Create row
        val rowParams = TableLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        row.layoutParams = rowParams

        //Create horizontal layout
        val rowLayout = LinearLayout(this.requireContext())
        rowLayout.orientation = LinearLayout.HORIZONTAL
        val params = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        rowLayout.layoutParams = params

        //Create ImageView
        val productImage = ImageView(this.requireContext())
        val imageParams = ViewGroup.LayoutParams(305, 305)
        productImage.layoutParams = imageParams
        Glide.with(this.requireContext()).load(itemCart.imageUrl).into(productImage)
        //Adding image to rowLayout
        rowLayout.addView(productImage)

        Log.d("CartItems", "Populate layout details")

        //Create LinearLayout vertical
        val detailsLayout = LinearLayout(this.requireContext())
        detailsLayout.orientation = LinearLayout.VERTICAL
        val detailLayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        detailsLayout.layoutParams = detailLayoutParams

        //Creating a textview to show the product name
        val productName = TextView(this.requireContext())
        productName.text = itemCart.product.name
        val productNameParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        productName.layoutParams = productNameParams
        val boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD)
        productName.setTextAppearance(R.style.TextAppearance_AppCompat_Medium)
        productName.typeface = boldTypeface

        //Creating a textview to show the product SKU
        val productSku = TextView(this.requireContext())
        productSku.text = itemCart.product.sku

        val productSkuParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        productSku.layoutParams = productSkuParams
        productSku.setTextAppearance(R.style.TextAppearance_AppCompat_Small)
        productSku.gravity = Gravity.END

        //Creating fixed "Details" TextView
        val detailsTextView = TextView(this.requireContext())
        detailsTextView.text = "Details:"
        val detailsTextViewParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        detailsTextView.layoutParams = detailsTextViewParams
        detailsTextView.setTextAppearance(R.style.TextAppearance_AppCompat_Small)

        //Creating details TextView
        val details = TextView(this.requireContext())
        details.text = "Size: ${itemCart.size}, Color: ${itemCart.color}"
        val detailsParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        details.layoutParams = detailsParams
        details.setTextAppearance(R.style.TextAppearance_AppCompat_Small)

        //Adding TextViews to layout
        detailsLayout.addView(productName)
        detailsLayout.addView(productSku)
        detailsLayout.addView(detailsTextView)
        detailsLayout.addView(details)

        //Create layout to show quantity
        val spinnerLayout = LinearLayout(this.requireContext())
        spinnerLayout.orientation = LinearLayout.VERTICAL
        val spinnerLayoutParams = LinearLayout.LayoutParams(
            200,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        spinnerLayoutParams.gravity = Gravity.CENTER
        spinnerLayout.layoutParams = spinnerLayoutParams

        //Create spinner
        val spinner = Spinner(this.requireContext())
        val personNames = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        val arrayAdapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, personNames)
        spinner.adapter = arrayAdapter
        spinner.setSelection(arrayAdapter.getPosition(itemCart.quantity.toString()))
        spinnerLayout.addView(spinner)

        Log.d("CartItems", "Populate price details")

        //Create layout to show price
        val priceLayout = LinearLayout(this.requireContext())
        priceLayout.orientation = LinearLayout.VERTICAL
        val priceLayoutParams = LinearLayout.LayoutParams(230,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        priceLayoutParams.gravity = Gravity.CENTER
        priceLayout.layoutParams = priceLayoutParams

        //Creating price TextView
        val price = TextView(this.requireContext())
        price.text = "$ ${itemCart.product.price} USD"
        val priceParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        price.layoutParams = priceParams
        price.setTextAppearance(R.style.TextAppearance_AppCompat_Small)
        price.gravity = Gravity.END

        priceLayout.addView(price)

        //Add all layouts to main layout
        rowLayout.addView(detailsLayout)
        rowLayout.addView(spinnerLayout)
        rowLayout.addView(priceLayout)

        //Add rowLayout to TableRow
        row.addView(rowLayout)

        //Add TableRow to table
        Log.d("CartItems", "Populate table row")

        val table = this.requireView().findViewById<TableLayout>(R.id.cartTable)
        table.addView(row)
    }

    private fun showEmptyCartView() {
        val layoutCart: LinearLayout = this.requireView().findViewById(R.id.cartLayout)
        layoutCart.visibility = View.INVISIBLE
        val emptyCart: LinearLayout = this.requireView().findViewById(R.id.emptyCart)
        emptyCart.visibility = View.VISIBLE
    }

    private fun showCartView() {
        val layoutCart: LinearLayout = this.requireView().findViewById(R.id.cartLayout)
        layoutCart.visibility = View.VISIBLE
        val emptyCart: LinearLayout = this.requireView().findViewById(R.id.emptyCart)
        emptyCart.visibility = View.INVISIBLE
    }

    private fun showCheckoutView(orderId: String) {
        val intent = Intent (activity, CheckoutActivity::class.java)
        intent.putExtra("orderId",orderId)
        activity?.startActivity(intent)
        parentFragmentManager.beginTransaction().remove(this).commit()
    }

}
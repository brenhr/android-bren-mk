package com.brenhr.mkonline.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.brenhr.mkonline.R
import com.brenhr.mkonline.model.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

import com.google.firebase.storage.ktx.storage
import com.brenhr.mkonline.util.*


class DetailsFragment : Fragment() {
    private lateinit var id: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var productParser: ProductParser
    private lateinit var userParser: UserParser
    private lateinit var orderParser: OrderParser
    private lateinit var storage: FirebaseStorage

    private lateinit var firestoreUser: User

    private lateinit var colorCatalog: MutableList<String>
    private lateinit var sizeCatalog: MutableList<String>

    private lateinit var colorParser: ColorParser
    private lateinit var sizeParser: SizeParser

    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        colorParser = ColorParser()
        sizeParser = SizeParser()

        productParser = ProductParser()
        userParser = UserParser()
        orderParser = OrderParser()

        auth = Firebase.auth
        storage = Firebase.storage
        firestore = Firebase.firestore

        colorCatalog = mutableListOf()
        sizeCatalog = mutableListOf()

        id = arguments?.getString("id")!!
        database = Firebase.database


        if(id.isNotEmpty()) {
            Log.d("DetailsFragment", "Product id: $id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkSession()

        val addToCartButton: Button = this.requireView().findViewById(R.id.addToCartButton)

        addToCartButton.setOnClickListener {
            addItemToCart()
        }
    }

    private fun checkSession() {
        val user = Firebase.auth.currentUser

        if (user != null) {
            Log.d("Authentication", "User ID: ${user.uid}")
            loadElements()
        } else {
            signInUserAnonymously()
        }
    }

    private fun signInUserAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user: FirebaseUser? = auth.currentUser
                    Log.d("AnonymousAuthentication","User id: ${user!!.uid}")
                    loadElements()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e("AnonymousAuthentication", "signInAnonymously:failure", task.exception)
                    loadElements()
                }
            }
    }

    private fun loadElements() {
        val myRef = database.getReference("Product")

        myRef.child(id).get().addOnSuccessListener {
            Log.i("database", "Got value ${it.value}")
            product = productParser.parser(it)
            getCatalogs(product)
            getImageFromStorage(product)

        }.addOnFailureListener{
            Log.e("database", "Error getting data", it)
        }
    }

    private fun getImageFromStorage(product: Product) {
        val storageRef = storage.reference
        val sku = product.sku.lowercase()
        val spaceRef = storageRef.child("clothes/$sku/$sku-main.png")
        Log.i("storage","URI: ${spaceRef.path}")
        spaceRef.downloadUrl.addOnSuccessListener {
            populateView(product, it.toString())
        }.addOnFailureListener {
            Log.e("storage", it.toString())
        }
    }

    private fun populateView(product: Product, imageUrl: String) {
        val textProductName: TextView = this.requireView().findViewById(R.id.productName)
        textProductName.text = product.name

        val productImage: ImageView = this.requireView().findViewById(R.id.productImage)
        Glide.with(this.requireContext()).load(imageUrl).into(productImage)

        val productPrice: TextView = this.requireView().findViewById(R.id.priceText)
        ("$ " + product.price + " USD").also { productPrice.text = it }

        val productModel: TextView = this.requireView().findViewById(R.id.modelText)
        productModel.text = product.sku

        val productDescription: TextView = this.requireView().findViewById(R.id.descriptionText)
        productDescription.text = product.description

        val spinnerQuantity: Spinner = this.requireView().findViewById(R.id.quantityList)

        val personNames = arrayOf("1", "2", "3", "4", "5")
        val arrayAdapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, personNames)

        spinnerQuantity.adapter = arrayAdapter

    }

    private fun getCatalogs(product: Product) {
        product.details.forEach {
            addColorToList(it.color)
            addSizeToList(it.size)
        }
    }

    private fun addColorToList(colorId: String) {
        Log.i("ColorDatabase", "ID: $colorId.")
        val myRef = database.getReference("Color")
        myRef.child(colorId).get().addOnSuccessListener {
            Log.i("ColorDatabase", "Got value ${it.value}")
            val c: Color = colorParser.parser(it)
            if(!colorCatalog.contains(c.name)) {
                this.colorCatalog.add(c.name)
            }

            val colorSpinner: Spinner = this.requireView().findViewById(R.id.colorList)
            val arrayAdapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, colorCatalog)

            colorSpinner.adapter = arrayAdapter
        }.addOnFailureListener{
            Log.e("ColorDatabase", "Error getting data", it)
        }
    }

    private fun addSizeToList(sizeId: String) {
        val myRef = database.getReference("Size")

        myRef.child(sizeId).get().addOnSuccessListener {
            Log.i("SizeDatabase", "Got value ${it.value}")
            val s: Size = sizeParser.parser(it)
            if(!this.sizeCatalog.contains(s.name)) {
                this.sizeCatalog.add(s.name)
            }

            val sizeSpiner: Spinner = this.requireView().findViewById(R.id.sizeList)
            val arrayAdapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, sizeCatalog)

            sizeSpiner.adapter = arrayAdapter

        }.addOnFailureListener{
            Log.e("SizeDatabase", "Error getting data", it)
        }
    }

    private fun showCart(orderId: String) {
        val cartFragment = CartFragment()

        val args = Bundle()
        args.putString("orderId", orderId)
        cartFragment.arguments = args

        val transaction = this.requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, cartFragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    private fun addItemToCart() {
        findUser()
    }

    private fun findUser() {
        val user = Firebase.auth.currentUser!!
        val userReference = firestore.collection("users").document(user.uid)

        userReference.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("FirestoreUser", "User data: ${document.data}")
                    firestoreUser = userParser.parser(document)
                    findUserCart()
                } else {
                    Log.d("FirestoreUser", "No such user")
                    createFirestoreAnonymousUser(user.uid)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("FirestoreUser", "Error getting user", exception)
            }
    }

    private fun findUserCart() {
        Log.d("FirestoreOrder", "Searching if there is an existing cart for this user")
        val user = Firebase.auth.currentUser
        val reference = firestore.collection("users").document(user!!.uid)
            .collection("orders")
        reference.whereEqualTo("status", "cart")
            .get()
            .addOnSuccessListener { documents ->
                if(!documents.isEmpty) {
                    for (document in documents) {
                        Log.d("FirestoreOrder", "Cart: ${document.id} => ${document.data}")
                        firestoreUser.cart = orderParser.parse(document)
                        getItems(user.uid, document.id)
                    }
                } else {
                    Log.d("FirestoreOrder", "Creating a new cart for user ${user.uid}")
                    createUserCart(user.uid)
                }

            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting user cart: ", exception)
            }
    }

    private fun getItems(uid: String, documentId: String) {
        //Get selected quantity
        val quantitySpinner = this.requireView().findViewById<Spinner>(R.id.quantityList)
        val quantity = quantitySpinner.selectedItem.toString().toLong()

        //Get selected size
        val sizeSpinner = this.requireView().findViewById<Spinner>(R.id.sizeList)
        val size = sizeSpinner.selectedItem.toString()

        //Get selected color
        val colorSpinner = this.requireView().findViewById<Spinner>(R.id.colorList)
        val color = colorSpinner.selectedItem.toString()

        //Find if there is already an item with same characteristics in user's cart
        val itemReference = firestore.collection("users").document(uid)
            .collection("orders").document(documentId).collection("items")
            .whereEqualTo("productId", product.id)
            .whereEqualTo("color", color)
            .whereEqualTo("size", size).limit(1)
        itemReference.get()
            .addOnSuccessListener { documents ->
                if(!documents.isEmpty) {
                    for (document in documents) {
                        Log.d("ProductItems", "Items: ${document.id} => ${document.data}")
                        val oldQuantity = document.data?.get("quantity") as Long

                        modifyOrder(uid, documentId, quantity, false, document.id, oldQuantity)
                    }
                } else {
                    modifyOrder(uid, documentId, quantity, true, null, null)
                }
        }
            .addOnFailureListener { exception ->
                Log.w("FirestoreItems", "Error getting cart items: ", exception)
            }
    }

    private fun modifyOrder(uid: String, orderId: String, quantity: Long, newItem: Boolean,
                            itemId: String?, oldQuantiy: Long?){
        val newQuantity = firestoreUser.cart!!.quantity + quantity
        val newPrice = firestoreUser.cart!!.total + product.price * quantity
        val order = Order("cart", newQuantity, newPrice)
        firestore.collection("users").document(uid).collection("orders")
            .document(orderId).set(order).addOnSuccessListener {
                Log.d("FirestoreOrder", "Order successfully modified!")
                if(newItem) {
                    createItem(uid, orderId, quantity)
                } else {
                    modifyItem(uid, orderId, itemId!!, quantity, oldQuantiy!!)
                }

            }.addOnFailureListener { e ->
                Log.w("Firestore", "Error modifying order", e)
            }
    }

    private fun modifyItem(uid: String, orderId: String, itemId: String, quantity: Long, oldQuantiy: Long) {
        val newQuantity = oldQuantiy + quantity
        firestore.collection("users").document(uid).collection("orders")
            .document(orderId).collection("items").document(itemId)
            .update("quantity", newQuantity).addOnSuccessListener {
                Log.d("Firestore", "Item successfully updated")
                showCart(orderId)
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Error updating item", e)
            }
    }

    private fun createFirestoreAnonymousUser(uid: String) {
        val profilePicture = "https://firebasestorage.googleapis.com/v0/b/bren-mk-android.appspot" +
                ".com/o/profile-pictures%2Fdefault%2FImage10.png?alt=media&token=5d722677-e225-" +
                "4aa4-8f35-1eb98b12bc1f"
        val user = User("Anonymous","Anonymous","Anonymous",profilePicture)
        firestore.collection("users").document(uid)
            .set(user).addOnSuccessListener {
                Log.d("Firestore", "User successfully created!")
                firestoreUser = user
                createUserCart(uid)
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Error creating user", e)
            }
    }

    private fun createUserCart(uid: String) {
        //Get selected quantity
        val quantitySpinner = this.requireView().findViewById<Spinner>(R.id.quantityList)
        val quantity = quantitySpinner.selectedItem.toString().toLong()

        //Get price
        val price = product.price

        //Calculate total order
        val total = price * quantity

        val order = Order("cart", quantity, total)

        createOrder(uid, order)

    }

    private fun createOrder(uid: String, order: Order) {
        firestore.collection("users").document(uid).collection("orders")
            .add(order).addOnSuccessListener {
                Log.d("Firestore", "Order successfully created!")
                id = it.id
                firestoreUser.cart = order
                createItem(uid, id, order.quantity)
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Error creating order", e)
            }
    }

    private fun createItem(uid: String, documentId: String, quantity: Long) {
        //Get selected size
        val sizeSpinner = this.requireView().findViewById<Spinner>(R.id.sizeList)
        val size = sizeSpinner.selectedItem.toString()

        //Get selected color
        val colorSpinner = this.requireView().findViewById<Spinner>(R.id.colorList)
        val color = colorSpinner.selectedItem.toString()

        //Get product id
        val productId = product.id

        //Add item to cart
        val item = Item(productId, quantity, color, size)
        addItem(uid, documentId, item)

    }

    private fun addItem(uid: String, documentId: String, item: Item) {
        firestore.collection("users").document(uid).collection("orders")
            .document(documentId).collection("items")
            .add(item).addOnSuccessListener {
                Log.d("Firestore", "Item successfully created!")
                id = it.id
                showCart(documentId)
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Error creating item", e)
            }
    }

}
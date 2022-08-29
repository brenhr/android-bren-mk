package com.brenhr.mkonline.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.brenhr.mkonline.R
import com.brenhr.mkonline.model.Order
import com.brenhr.mkonline.model.User
import com.brenhr.mkonline.util.OrderParser
import com.brenhr.mkonline.util.UserParser
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast

import android.graphics.BitmapFactory

import java.io.InputStream
import java.lang.Exception


class MyProfileFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var userParser: UserParser
    private lateinit var userInfo: User
    private lateinit var orderParser: OrderParser

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

        userParser = UserParser()
        orderParser = OrderParser()

        user = auth.currentUser!!
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUserInfo()
        getOrders()

        val logoutButton = this.requireView().findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            logout()
        }

        val imageProfilePicture = this.requireView().findViewById<ImageView>(R.id.profilePicture)
        imageProfilePicture.setOnClickListener {
            loadImagePicture()
        }
    }

    private fun getUserInfo() {
        val userRef = firestore.collection("users").document(user.uid)

        userRef.get().addOnSuccessListener {
            getProfilePicture(it)
        }
    }

    private fun getOrders() {
        val userRef = firestore.collection("users").document(user.uid)
            .collection("orders").whereNotEqualTo("status", "cart")

            userRef.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("FirestoreOrders", "${document.id} => ${document.data}")
                    val order = orderParser.parse(document)
                    populateOrder(order)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreOrders", "Error getting documents: ", exception)
            }
    }

    private fun getProfilePicture(userSnapshot: DocumentSnapshot) {
        val profilePictureRef = storage.reference
            .child("profile-pictures/${user.uid}/${user.uid}.jpg")
        profilePictureRef.downloadUrl.addOnSuccessListener {
            userInfo = userParser.parser(userSnapshot, it.toString())
            populateUserInfo()
        }.addOnFailureListener {
            val profilePicture = "https://firebasestorage.googleapis.com/v0/b/bren-mk-android.appspot" +
                    ".com/o/profile-pictures%2Fdefault%2FImage10.png?alt=media&token=5d722677-e225-" +
                    "4aa4-8f35-1eb98b12bc1f"
            userInfo = userParser.parser(userSnapshot, profilePicture)
            populateUserInfo()
        }

    }

    private fun populateUserInfo() {
        val textName = this.requireView().findViewById<TextView>(R.id.textName)
        val textLastName = this.requireView().findViewById<TextView>(R.id.textLastName)
        val textEmail = this.requireView().findViewById<TextView>(R.id.textUserEmail)
        val profilePicture = this.requireView().findViewById<ImageView>(R.id.profilePicture)

        textName.text = userInfo.name
        textLastName.text = userInfo.lastName
        textEmail.text = userInfo.email


        Glide.with(this.requireContext()).load(userInfo.profilePicture).into(profilePicture)
    }

    private fun populateOrder(order: Order) {
        val row = TableRow(this.requireContext())

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

        //Order ID text view
        val orderIdText = TextView(this.requireContext())
        orderIdText.text = order.id!!.substring(0,13)
        val orderIdTextParams = LinearLayout.LayoutParams(
            490,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        orderIdText.layoutParams = orderIdTextParams
        val boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD)
        orderIdText.setTextAppearance(R.style.TextAppearance_AppCompat_Medium)
        orderIdText.typeface = boldTypeface

        //Items text view
        val itemsText = TextView(this.requireContext())
        itemsText.text = order.quantity.toString()
        val itemsTextParams = LinearLayout.LayoutParams(
            120,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        itemsText.layoutParams = itemsTextParams
        itemsText.setTextAppearance(R.style.TextAppearance_AppCompat_Medium)

        //Price text view
        val priceText = TextView(this.requireContext())
        "$ ${order.total}".also { priceText.text = it }
        val priceTextParams = LinearLayout.LayoutParams(
            170,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        priceText.layoutParams = priceTextParams
        priceText.setTextAppearance(R.style.TextAppearance_AppCompat_Medium)

        //Status textView
        val statusText = TextView(this.requireContext())
        statusText.text = order.status
        val statusTextParams = LinearLayout.LayoutParams(
            250,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        statusText.layoutParams = statusTextParams
        statusText.setTextAppearance(R.style.TextAppearance_AppCompat_Medium)

        //Add text views to layout
        rowLayout.addView(orderIdText)
        rowLayout.addView(itemsText)
        rowLayout.addView(priceText)
        rowLayout.addView(statusText)

        //Add layout to row
        row.addView(rowLayout)

        //Add row to table
        val table = this.requireView().findViewById<TableLayout>(R.id.tableOrders)
        table.addView(row)

    }

    private fun loadImagePicture() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        activityResultLauncher.launch(intent)
    }

    private var activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult())
        { result ->
            showProgress()
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val selectedImageUri: Uri = data.data!!
                    try {
                        val inputStream: InputStream =
                            this.requireContext().contentResolver.openInputStream(selectedImageUri)!!
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        uploadImageToStorage(bitmap)
                    } catch (e: Exception) {
                        Log.e("Storage", "Image couldn't be uploaded", e)
                        Toast.makeText(this.requireContext(), "Couldn't upload image. Please try again later",
                            Toast.LENGTH_SHORT).show()
                        hideProgress()
                    }
                }

            }
        }

    private fun uploadImageToStorage(bitmap: Bitmap) {
        val ref = storage.reference.child("profile-pictures/${user.uid}/${user.uid}.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        ref.putBytes(data).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { url ->
                val newPicture = this.requireView().findViewById<ImageView>(R.id.profilePicture)
                Glide.with(this.requireContext()).load(url.toString()).into(newPicture)
                hideProgress()
            }
        }.addOnFailureListener { ex ->
            Log.e("Storage", "Failed to upload image to storage", ex)
            hideProgress()
            Toast.makeText(this.requireContext(), "Couldn't upload image. Please try again later",
                Toast.LENGTH_SHORT).show()
        }

    }

    private fun logout() {
        Firebase.auth.signOut()

        val homeFragment = HomeFragment()

        val transaction = this.requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, homeFragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    private fun showProgress() {
        val progressBar: RelativeLayout = this.requireView().findViewById(R.id.loadingPanel)
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        val progressBar: RelativeLayout = this.requireView().findViewById(R.id.loadingPanel)
        progressBar.visibility = View.INVISIBLE
    }
}
package com.brenhr.mkonline.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.brenhr.mkonline.R
import com.brenhr.mkonline.model.Color
import com.brenhr.mkonline.model.Size
import com.brenhr.mkonline.model.Product
import com.brenhr.mkonline.util.ColorParser
import com.brenhr.mkonline.util.ProductParser
import com.brenhr.mkonline.util.SizeParser
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

import android.widget.Spinner

import android.widget.ArrayAdapter
import com.brenhr.mkonline.model.ProductDetail
import com.google.firebase.storage.ktx.storage


class DetailsFragment : Fragment() {
    private lateinit var id: String
    private lateinit var database: FirebaseDatabase
    private lateinit var productService: ProductParser
    private lateinit var storage: FirebaseStorage

    private lateinit var colorCatalog: MutableList<String>
    private lateinit var sizeCatalog: MutableList<String>

    private lateinit var colorParser: ColorParser
    private lateinit var sizeParser: SizeParser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        colorParser = ColorParser()
        sizeParser = SizeParser()

        productService = ProductParser()
        storage = Firebase.storage

        colorCatalog = mutableListOf()
        sizeCatalog = mutableListOf()

        id = arguments?.getString("id")!!
        database = Firebase.database


        if(id!!.isNotEmpty()) {
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
        loadElements()
    }

    private fun loadElements() {
        val myRef = database.getReference("Product")

        myRef.child(id).get().addOnSuccessListener {
            Log.i("database", "Got value ${it.value}")
            val p = productService.parser(it)
            getCatalogs(p)
            getImageFromStorage(p)

        }.addOnFailureListener{
            Log.e("database", "Error getting data", it)
        }
    }

    private fun getImageFromStorage(product: Product) {
        val storageRef = storage.reference
        val sku = product.sku!!.lowercase()
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
        productPrice.text = "$ ${product.price} USD"

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
            var c: Color = colorParser.parser(it)
            if(!colorCatalog.contains(c.name)) {
                this.colorCatalog.add(c.name)
            }

            var colorSpinner: Spinner = this.requireView().findViewById(R.id.colorList)
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
            var s: Size = sizeParser.parser(it)
            if(!this.sizeCatalog.contains(s.name)) {
                this.sizeCatalog.add(s.name)
            }

            var sizeSpiner: Spinner = this.requireView().findViewById(R.id.sizeList)
            val arrayAdapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, sizeCatalog)

            sizeSpiner.adapter = arrayAdapter

        }.addOnFailureListener{
            Log.e("SizeDatabase", "Error getting data", it)
        }
    }

}
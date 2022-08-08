package com.brenhr.mkonline.fragment

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import com.brenhr.mkonline.R
import com.brenhr.mkonline.database.ProductParser
import com.brenhr.mkonline.model.Product
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.graphics.Typeface
import com.bumptech.glide.Glide
import com.google.firebase.storage.ktx.storage

class HomeFragment : Fragment() {
    private val productService: ProductParser = ProductParser()
    private val storage = Firebase.storage
    private val database = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        populateHome()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {

            }
    }

    private fun populateHome() {
        getAllProducts()
    }

    private fun getAllProducts() {
        val myRef = database.getReference("Product")
        myRef.get().addOnSuccessListener {
            it.children.forEach { child ->
                Log.i("database", "Got value ${child.value}")
                val p = productService.parser(child)
                getImageFromStorage(p)
            }
        }.addOnFailureListener{
            Log.e("database", "Error getting data", it)
        }
        Log.i("database", "Returning list")
    }

    private fun getImageFromStorage(product: Product) {
        val storageRef = storage.reference
        val sku = product.sku!!.lowercase()
        val spaceRef = storageRef.child("clothes/$sku/$sku-main.png")
        Log.i("storage","URI: ${spaceRef.path}")
        spaceRef.downloadUrl.addOnSuccessListener {
            createCardView(product, it.toString())
        }.addOnFailureListener {
            Log.e("storage", it.toString())
        }
    }

    private fun createCardView(product: Product, imageUrl: String) {
        Log.i("UI","Product: " + product.sku)
        val productCard = CardView(this.requireContext())
        val productCardParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 700)
        productCard.layoutParams = productCardParams
        val mainLayout = this.view?.findViewById<LinearLayout>(R.id.mainLayout)

        //Main space betweeen one card and another
        val cardSpace = Space(this.context)
        val cardSpaceParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            30)
        cardSpace.layoutParams = cardSpaceParams

        //Creating main card layout (will divide image and product details)
        val cardLayout = LinearLayout(this.requireContext())
        cardLayout.orientation = LinearLayout.HORIZONTAL
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        cardLayout.layoutParams = params
        //Creating imageview
        val productImage = ImageView(this.requireContext())
        val imageParams = ViewGroup.LayoutParams(600, 550)
        productImage.layoutParams = imageParams
        Glide.with(this.requireContext()).load(imageUrl).into(productImage);
        //Adding image to cardlayout
        cardLayout.addView(productImage)

        //Creating a vertical layout, which will be useful to order the product details
        val productDetailLayout = LinearLayout(this.requireContext())
        productDetailLayout.orientation = LinearLayout.VERTICAL
        val productDetailLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        productDetailLayout.layoutParams = productDetailLayoutParams

        //Creating a textview to show the product name
        val productName = TextView(this.requireContext())
        productName.text = product.name
        val productNameParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        productName.layoutParams = productNameParams
        val boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD)

        productName.setTextAppearance(R.style.TextAppearance_AppCompat_Medium)
        productName.typeface = boldTypeface

        //Creating a textview to show the product SKU
        val productSku = TextView(this.requireContext())
        "SKU: ${product.sku}".also { productSku.text = it }
        val productSkuParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        productSku.layoutParams = productSkuParams
        productSku.setTextAppearance(R.style.TextAppearance_AppCompat_Small)

        val spacePrice = Space(this.context)
        val spacePriceParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            60)
        spacePrice.layoutParams = spacePriceParams

        //Creating a textview to show the product price
        val price = TextView(this.requireContext())
        "$ ${product.price} ${product.currency}".also { price.text = it }
        val priceParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        price.layoutParams = priceParams
        Gravity.END.also { price.gravity = it }
        price.setTextAppearance(R.style.TextAppearance_AppCompat_Medium)

        val spaceButton = Space(this.context)
        val spaceParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            100)
        spaceButton.layoutParams = spaceParams

        val detailsButton = Button(this.requireContext())
        val buttonParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        detailsButton.layoutParams = buttonParams
        detailsButton.text = getString(R.string.showDetailsText)

        //Add elements to layout
        productDetailLayout.addView(productName)
        productDetailLayout.addView(productSku)
        productDetailLayout.addView(spacePrice)
        productDetailLayout.addView(price)
        productDetailLayout.addView(spaceButton)
        productDetailLayout.addView(detailsButton)

        //add product detail layout to the card layout
        cardLayout.addView(productDetailLayout)

        //add cardlayout to cardView
        productCard.addView(cardLayout)

        //add card layout to main layout
        mainLayout!!.addView(cardSpace)
        mainLayout.addView(productCard)
    }
}
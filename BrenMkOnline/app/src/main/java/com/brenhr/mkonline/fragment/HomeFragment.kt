package com.brenhr.mkonline.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.brenhr.mkonline.R
import com.brenhr.mkonline.database.ProductService
import com.brenhr.mkonline.model.Product
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    private var productList = mutableListOf<Product>()

    private val productService: ProductService = ProductService()

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

    fun getAllProducts() {
        val database = Firebase.database
        val myRef = database.getReference("Product")
        myRef.get().addOnSuccessListener {
            it.children.forEach { child ->
                Log.i("database", "Got value ${child.value}")
                createCardView(productService.parser(child))
            }
        }.addOnFailureListener{
            Log.e("database", "Error getting data", it)
        }
        Log.i("database", "Returning list")
    }

    private fun createCardView(product: Product) {
        Log.i("UI","Product: " + product.sku)
        val productCard = CardView(this.requireContext())
        val productCardParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 700)
        productCard.layoutParams = productCardParams
        val mainLayout = this.view?.findViewById<LinearLayout>(R.id.mainLayout)

        //Creating main card layout (will divide image and product details)
        val cardLayout = LinearLayout(this.requireContext())
        cardLayout.orientation = LinearLayout.HORIZONTAL
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        cardLayout.layoutParams = params
        //Creating imageview
        val productImage = ImageView(this.requireContext())
        val imageParams = ViewGroup.LayoutParams(
            600,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        productImage.layoutParams = imageParams
        productImage.setImageResource(R.drawable.skirt)
        //Adding image to cardlayout
        cardLayout.addView(productImage)

        //Creating a vertical layout, which will be usefull to order the product details
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
        productName.setTextAppearance(R.style.TextAppearance_AppCompat_Medium)

        //Creating a textview to show the product SKU
        val productSku = TextView(this.requireContext())
        productSku.text = "SKU: " + product.sku
        val productSkuParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        productSku.layoutParams = productSkuParams
        productSku.setTextAppearance(R.style.TextAppearance_AppCompat_Small)

        //Creating a textview to show the product price
        val price = TextView(this.requireContext())
        price.text = "$ " + product.price + " " + product.currency
        val priceParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        price.layoutParams = priceParams
        price.gravity = Gravity.RIGHT
        price.setTextAppearance(R.style.TextAppearance_AppCompat_Medium)

        val detailsButton = Button(this.requireContext())
        val buttonParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        detailsButton.layoutParams = buttonParams
        detailsButton.text = "Show details"

        //Add elements to layout
        productDetailLayout.addView(productName)
        productDetailLayout.addView(productSku)
        productDetailLayout.addView(price)
        productDetailLayout.addView(detailsButton)

        //add product detail layout to the card layout
        cardLayout.addView(productDetailLayout)

        //add cardlayout to cardView
        productCard.addView(cardLayout)

        //add card layout to main layout
        mainLayout!!.addView(productCard)

    }
}
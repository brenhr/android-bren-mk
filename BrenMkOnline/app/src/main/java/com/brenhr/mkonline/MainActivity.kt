package com.brenhr.mkonline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.brenhr.mkonline.fragment.CartFragment
import com.brenhr.mkonline.fragment.HomeFragment
import com.brenhr.mkonline.fragment.LoginFragment
import com.brenhr.mkonline.fragment.MyProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var homeFragment: HomeFragment
    private lateinit var cartFragment: CartFragment
    private lateinit var myProfileFragment: MyProfileFragment
    private lateinit var loginFragment: LoginFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        homeFragment = HomeFragment()
        cartFragment = CartFragment()
        myProfileFragment = MyProfileFragment()
        loginFragment = LoginFragment()

        setContentView(R.layout.activity_main)

        loadFragment(homeFragment)

        val navigationMenu = findViewById<BottomNavigationView>(R.id.navigation_menu)

        navigationMenu.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    loadFragment(homeFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.cartFragment -> {
                    loadFragment(cartFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.profileFragment -> {
                    checkForUserSession()
                    return@setOnItemSelectedListener true
                }
                else -> {
                    loadFragment(homeFragment)
                    return@setOnItemSelectedListener true
                }
            }
        }
    }

    private fun checkForUserSession() {
        val user = Firebase.auth.currentUser
        if (user == null || user.isAnonymous) {
            // User is signed in
            loadFragment(loginFragment)
        } else {
            // No user is signed in
            loadFragment(myProfileFragment)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_container, fragment)
        fragmentTransaction.commit()
    }
}
package com.brenhr.mkonline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.brenhr.mkonline.fragment.CartFragment
import com.brenhr.mkonline.fragment.HomeFragment
import com.brenhr.mkonline.fragment.LoginFragment
import com.brenhr.mkonline.fragment.MyProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val homeFragment = HomeFragment();
    private val cartFragment = CartFragment();
    private val myProfileFragment = MyProfileFragment();
    private val loginFragment = LoginFragment();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(homeFragment)

        val navigationMenu = findViewById<BottomNavigationView>(R.id.navigation_menu);
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
                    loadFragment(loginFragment)
                    return@setOnItemSelectedListener true
                }
                else -> {
                    loadFragment(homeFragment)
                    return@setOnItemSelectedListener true
                }
            }
        };
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_container, fragment)
        fragmentTransaction.commit()
    }
}
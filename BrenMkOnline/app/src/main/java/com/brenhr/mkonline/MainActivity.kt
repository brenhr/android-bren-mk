package com.brenhr.mkonline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    val homeFragment = HomeFragment();
    val cartFragment = CartFragment();
    val myProfileFragment = MyProfileFragment();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navigationMenu = findViewById<BottomNavigationView>(R.id.homeFragment);
        navigationMenu.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    loadFragment(HomeFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.cartFragment -> {
                    loadFragment(CartFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.profileFragment -> {
                    loadFragment(MyProfileFragment())
                    return@setOnItemSelectedListener true
                }
                else -> {
                    loadFragment(MyProfileFragment())
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
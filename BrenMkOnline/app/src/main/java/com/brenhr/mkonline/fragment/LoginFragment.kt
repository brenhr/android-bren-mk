package com.brenhr.mkonline.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.brenhr.mkonline.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginButton = this.requireView().findViewById(R.id.loginButton)
        signUpButton = this.requireView().findViewById(R.id.signUpButton)

        loginButton.setOnClickListener{
            val email = this.requireView().findViewById<EditText>(R.id.email).text.toString()
            val password = this.requireView().findViewById<EditText>(R.id.password).text.toString()

            Log.d("passwordAuthentication", "$email $password")

            if(email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("PasswordAuthentication", "signInWithEmail:success")
                            val user: FirebaseUser = auth.currentUser!!
                            Log.d("PasswordAuthentication", "User UID: ${user.uid}")
                            showMyProfile()
                        } else {
                            Log.w("PasswordAuthentication", "signInWithEmail:failure",
                                task.exception)
                            Toast.makeText(this.requireContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this.requireContext(), "Please fill out all the fields.",
                    Toast.LENGTH_SHORT).show()
            }
        }

        signUpButton.setOnClickListener {
            val email = this.requireView().findViewById<EditText>(R.id.email).text.toString()
            showRegisterView(email)
        }

    }

    private fun showMyProfile() {
        val transaction = this.requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, MyProfileFragment())
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    private fun showRegisterView(email: String) {
        val registerFragment = RegisterFragment()

        val args = Bundle()
        if(email.isNotEmpty()) {
            args.putString("email", email)
            registerFragment.arguments = args
        }

        val transaction = this.requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, registerFragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

}
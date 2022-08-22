package com.brenhr.mkonline.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.brenhr.mkonline.R
import com.brenhr.mkonline.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        db = Firebase.firestore

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        val email: String? = arguments?.getString("email")
        if(email != null && email.isNotEmpty()) {
            val emailEditText: EditText = this.requireView().findViewById(R.id.emailEditText)
            emailEditText.setText(email)
        }

        val registerButton = this.requireView().findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = this.requireView().findViewById<EditText>(R.id.emailEditText)
                .text.toString()
            val password = this.requireView().findViewById<EditText>(R.id.editTextPassword)
                .text.toString()
            val confirmPassword = this.requireView().findViewById<EditText>(R.id.editTextConfirmPassword)
                .text.toString()
            val name = this.requireView().findViewById<EditText>(R.id.editTextName)
                .text.toString()
            val lastName = this.requireView().findViewById<EditText>(R.id.editTextLastName)
                .text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
                && name.isNotEmpty() && lastName.isNotEmpty()) {
                registerUser(email, password, name, lastName)
            }
        }
    }

    private fun showMyProfile() {
        val transaction = this.requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, MyProfileFragment())
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    private fun registerUser(email: String, password: String, name: String, lastName: String) {
        var user: FirebaseUser? = Firebase.auth.currentUser
        if(user != null && user.isAnonymous) {
            //Link anonymous user with an existing account
            val credential = EmailAuthProvider.getCredential(email, password)
            auth.currentUser!!.linkWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("linkWitCredential", "linkWithCredential:success")
                        user = task.result?.user
                        registerFirestoreUser(user!!.uid, name, lastName, email)
                    } else {
                        Log.w("linkWithCredential", "linkWithCredential:failure", task.exception)
                        Toast.makeText(this.requireContext(), "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("passwordAuthentication", "createUserWithEmail:success")
                        user = auth.currentUser
                        registerFirestoreUser(user!!.uid, name, lastName, email)
                    } else {
                        Log.w("passwordAuthentication", "createUserWithEmail:failure",
                            task.exception)
                        Toast.makeText(this.requireContext(), "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun registerFirestoreUser(uid: String, name: String, lastName: String, email: String) {
        val profilePicture = "https://firebasestorage.googleapis.com/v0/b/bren-mk-android.appspot" +
                ".com/o/profile-pictures%2Fdefault%2FImage10.png?alt=media&token=5d722677-e225-" +
                "4aa4-8f35-1eb98b12bc1f"
        val user = User(name, lastName, email, profilePicture)
        db.collection("users").document(uid)
            .set(user).addOnSuccessListener {
                Log.d("Firestore", "User successfully created!")
                sendVerificationEmail()
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Error writing document", e)
            }

    }

    private fun sendVerificationEmail() {
        val user = Firebase.auth.currentUser
        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this.requireContext(), "We have sent a verification mail " +
                            "to your account. please check your mail inbox",
                        Toast.LENGTH_SHORT).show()
                    showMyProfile()
                }
            }.addOnFailureListener {
                Log.w("authentication", "Error sending verification email")
            }
    }
}
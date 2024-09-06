package com.example.aiimshealthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class LoginWithUsername : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val registerButton = findViewById<Button>(R.id.btnLogin)
        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginEmail = findViewById<Button>(R.id.loginEmail)


        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trimEnd()
            val email = "$username@yourapp.com" // Create a pseudo email
            val password = passwordEditText.text.toString().trimEnd()
            checkAndRegisterOrLogin(username, email, password)
        }

        loginEmail.setOnClickListener {
            val intent = Intent(this, LoginWithEmail::class.java)
            startActivity(intent)
        }

    }

    private fun checkAndRegisterOrLogin(username: String, email: String, password: String) {
        setInProgress(true)
        firestore.collection("users").whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { document ->
                if (document.isEmpty) {
                    registerUser(username, email, password)
                } else {
                    loginUser(email, password)

                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, getString(R.string.error_checking_username),
                    Toast.LENGTH_SHORT).show()
                setInProgress(false)
            }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, getString(R.string.login_successful),
                        Toast.LENGTH_SHORT).show()
                    setInProgress(false)
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                    setInProgress(false)
                    // Navigate to next activity
                } else {
                    Toast.makeText(baseContext, getString(R.string.invalid_credentials),
                        Toast.LENGTH_SHORT).show()
                    setInProgress(false)
                }
            }
    }

    private fun registerUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Successfully created the user
                    val currentUser = auth.currentUser

                    // Update the display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileUpdateTask ->
                            if (profileUpdateTask.isSuccessful) {
                                // Save username and email to Firestore
                                val user = hashMapOf(
                                    "username" to username,
                                    "email" to email
                                )
                                firestore.collection("users").document(currentUser.uid).set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(baseContext, getString(R.string.registration_successful),
                                            Toast.LENGTH_SHORT).show()
                                        // Navigate to login activity or next activity
                                        val intent = Intent(this, SocioDemographic::class.java)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(baseContext, getString(R.string.registration_failed),
                                            Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // Handle profile update failure
                                Toast.makeText(baseContext, getString(R.string.profile_update_failed),
                                    Toast.LENGTH_SHORT).show()
                            }
                            setInProgress(false)
                        }
                } else {
                    // Handle user creation failure
                    Toast.makeText(baseContext, getString(R.string.registration_failed),
                        Toast.LENGTH_SHORT).show()
                    setInProgress(false)
                }
            }
    }


    private fun setInProgress(progress:Boolean){
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        if(progress){
            progressBar.visibility = View.VISIBLE
        }
        else{
            progressBar.visibility = View.GONE
        }
    }
}

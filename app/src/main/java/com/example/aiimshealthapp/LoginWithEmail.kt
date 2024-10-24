package com.example.aiimshealthapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore


class LoginWithEmail : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var isPasswordVisible = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val editTextEmail = findViewById<EditText>(R.id.email)
        val editTextPassword = findViewById<EditText>(R.id.password)
        val editTextConfirmPassword = findViewById<EditText>(R.id.confirmPassword)

        val buttonReg = findViewById<Button>(R.id.btnRegister)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val loginUsername = findViewById<TextView>(R.id.loginUsername)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        loginUsername.setOnClickListener {
            val intent = Intent(this, LoginWithUsername::class.java)
            startActivity(intent)
        }
        buttonReg.setOnClickListener {
            val email = editTextEmail.text.toString().trimEnd()
            val password = editTextPassword.text.toString().trimEnd()
            val confirmPassword = editTextConfirmPassword.text.toString().trimEnd()
            if( email.isEmpty()){
                editTextEmail.error = "Email is required"
            }
            if( password.isEmpty()){
                editTextPassword.error = "Password is required"
            }
            if(password.length < 8){
                editTextPassword.error = "Password too short"
            }
            if(password != confirmPassword){
                editTextConfirmPassword.error = "Passwords don't match"
            }
            if(email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword)checkAndRegisterOrLogin(email.substringBefore("@"), email, password)
        }

        val showPasswordIcon = findViewById<ImageView>(R.id.showPasswordIcon)
        val showConfirmPasswordIcon = findViewById<ImageView>(R.id.showConfirmPasswordIcon)

        showPasswordIcon.setOnClickListener {
            // Toggle password visibility
            if (isPasswordVisible) {
                editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                showPasswordIcon.setImageResource(R.drawable.ic_visibility_off) // Update icon to "hidden"
            } else {
                editTextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                showPasswordIcon.setImageResource(R.drawable.ic_visibility) // Update icon to "visible"
            }
            isPasswordVisible = !isPasswordVisible
            editTextPassword.setSelection(editTextPassword.text.length)
        }
        showConfirmPasswordIcon.setOnClickListener {
            // Toggle password visibility
            if (isPasswordVisible) {
                editTextConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                showConfirmPasswordIcon.setImageResource(R.drawable.ic_visibility_off) // Update icon to "hidden"
            } else {
                editTextConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                showConfirmPasswordIcon.setImageResource(R.drawable.ic_visibility) // Update icon to "visible"
            }
            isPasswordVisible = !isPasswordVisible
            editTextConfirmPassword.setSelection(editTextConfirmPassword.text.length)
        }

    }
    private fun checkAndRegisterOrLogin(username: String, email: String, password: String) {
        setInProgress(true)
        firestore.collection("users").whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { document ->
                if (document.isEmpty) {
                    registerUser(username, email, password)
                } else {
                    Toast.makeText(baseContext, "This email is already in use",
                        Toast.LENGTH_SHORT).show()
                    setInProgress(false)
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
//                                        Toast.makeText(baseContext, getString(R.string.registration_successful),
//                                            Toast.LENGTH_SHORT).show()
                                        // Navigate to login activity or next activity
                                        val intent = Intent(this, OnboardingActivity::class.java)
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
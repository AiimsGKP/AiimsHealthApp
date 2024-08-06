package com.example.aiimshealthapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class LoginWithEmail : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val editTextEmail = findViewById<EditText>(R.id.email)
        val editTextPassword = findViewById<EditText>(R.id.password)

        val buttonReg = findViewById<Button>(R.id.btnRegister)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        auth = FirebaseAuth.getInstance()

        buttonReg.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

//            if(TextUtils.isEmpty(email)){
//                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//            if(TextUtils.isEmpty(password)){
//                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }

            if(!(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))){
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            progressBar.visibility = View.GONE

                            Toast.makeText(
                                this,
                                "Register Successfull",
                                Toast.LENGTH_SHORT,
                            ).show()
                            val intent = Intent(this, Dashboard::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            progressBar.visibility = View.VISIBLE
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        progressBar.visibility = View.GONE
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(
                                            this,
                                            "Login Successfull",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                        val user = auth.currentUser
                                        val intent = Intent(this, Dashboard::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        progressBar.visibility = View.GONE
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(
                                            this,
                                            "Invalid Credentials",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                }

                        }
                    }
            }
            else{
                val phoneNumber = findViewById<EditText>(R.id.phoneNumber)
//                if(phoneNumber.text.toString().length != 10){
//                    Toast.makeText(
//                        this,
//                        "Phone Number not valid!",
//                        Toast.LENGTH_SHORT,
//                    ).show()
//                    progressBar.visibility = View.GONE
//                    return@setOnClickListener
//                }

                val intent = Intent(this, OtpActivity::class.java)
                intent.putExtra("phone", phoneNumber.text.toString())
                startActivity(intent)
                finish()
//                sendVerificationCode(phoneNumber.text.toString())
            }
            progressBar.visibility = View.GONE
        }

    }
}
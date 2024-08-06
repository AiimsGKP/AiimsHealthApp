package com.example.aiimshealthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aiimshealthapp.utils.AndroidUtil
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {
    private val timeOut: Long = 60
    private val mAuth = FirebaseAuth.getInstance()
    private val androidUtil = AndroidUtil()
    private var verificationCode: String = ""
    private lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        val phoneNumber = intent.getStringExtra("phone").toString()
        sendOtp(phoneNumber, false)

        val otpBtn = findViewById<Button>(R.id.btnSubmitOTP)
        otpBtn.setOnClickListener {
            val otpInput = findViewById<EditText>(R.id.otpInput).text.toString()
            if (otpInput.isNotEmpty()) {
                verifyOtp(otpInput)
            } else {
                androidUtil.showToast(applicationContext, "Please enter the OTP")
            }
        }
    }

    private fun sendOtp(phoneNumber: String, isResend: Boolean) {
        setInProgress(true)
        val builder = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)           // Phone number to verify
            .setTimeout(timeOut, TimeUnit.SECONDS) // Timeout duration
            .setActivity(this)                     // Activity (for callback binding)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Handle verification completion
                    signIn(credential)
                    setInProgress(false)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Handle verification failure
                    androidUtil.showToast(applicationContext, "OTP Verification Failed")
                    setInProgress(false)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // Save verification ID and resending token so we can use them later
                    verificationCode = verificationId
                    resendingToken = token
                    androidUtil.showToast(applicationContext, "OTP Sent Successfully")
                    setInProgress(false)
                }
            })

        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build())
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        }
    }

    private fun verifyOtp(otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationCode, otp)
        signIn(credential)
    }

    private fun setInProgress(inProgress: Boolean) {
        val otpBtn = findViewById<Button>(R.id.btnSubmitOTP)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        if (inProgress) {
            progressBar.visibility = View.VISIBLE
            otpBtn.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            otpBtn.visibility = View.VISIBLE
        }
    }

    private fun signIn(credential: PhoneAuthCredential) {
        setInProgress(true)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    androidUtil.showToast(applicationContext, "Authentication Successful")
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                    // Proceed to the next activity
                } else {
                    androidUtil.showToast(applicationContext, "Authentication Failed")
                }
            }
        setInProgress(false)
    }
}
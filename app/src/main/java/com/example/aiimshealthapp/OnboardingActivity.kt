package com.example.aiimshealthapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aiimshealthapp.Fragments.SocioDemographicFragment
import com.example.aiimshealthapp.DummyFragment
import com.example.aiimshealthapp.Fragments.HealthHistoryFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.aiimshealthapp.models.Metrics
import com.example.aiimshealthapp.User
import com.example.aiimshealthapp.models.MetricsData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OnboardingActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val tag = "CHECK_RESPONSE"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)
        if(currentUser != null) {
            val user = User(username = currentUser.displayName.toString(), email = currentUser.email.toString())
            val metricsData = MetricsData(
                firstName = "",
                lastName = "",
                gender = "",
                age = "",
                educationLevel = "",
                employmentStatus = "",
                height = "",
                weight = "",
                bmi = "",
                waist = "",
                smoking = false,
                alcohol = false,
                diabetes = "",
                hypertension = "",
                medication = "",
                sleep = "",
                diet = "",
                stepGoal = "-1",
                steps = listOf(0, 0, 0, 0, 0, 0, 0)
            )

            val metrics = Metrics(user, metricsData)

            db.collection("metrics")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    Log.i(tag, "${documents.size()}")
                    if (documents != null && documents.size() > 0) {
                        for (document in documents) {
                            val documentId = document.id
                            val data = document.data
                            val updates = mapOf("metrics" to metricsData)
                            db.collection("metrics").document(documentId)
                                .update(updates)
                                .addOnSuccessListener {
                                    Log.d(tag, "Metrics updated successfully.")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(tag, "Error updating metrics", e)
                                }
                        }
                    }
                    else{
                        db.collection("metrics")
                            .add(metrics.toMap())
                            .addOnSuccessListener { documentReference ->
                                // Successfully added document
                                Log.i(tag,"DocumentSnapshot added with ID: ${documentReference.id}")
                            }
                            .addOnFailureListener { e ->
                                // Failed to add document
                                Log.i(tag,"Error adding document: $e")
                            }
                    }
                    if (savedInstanceState == null) {
                        val fragment = SocioDemographicFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout2, fragment) // Use replace or add
                            .commit()
                    }
                }

        }
    }
}

package com.example.aiimshealthapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.aiimshealthapp.models.Diseases
import com.example.aiimshealthapp.models.Fact
import com.example.aiimshealthapp.models.HealthEducationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DataUploading : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val tag = "CHECK_RESPONSE"
    private val collection = "health_education"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_uploading)
        uploadData()
    }

    private fun uploadData() {
        val facts = listOf(
                Diseases("Diabetes",
                    "Diabetes is a chronic medical condition where the body is unable to properly regulate blood sugar (glucose) levels. This happens due to either insufficient insulin production (Type 1 diabetes) or the body's inability to effectively use insulin (Type 2 diabetes).",
                    "These are the causes",
                    "These are the preventions",
                    "These are the symptoms",
                    "Here is what you have to eat"),
                )
        val data = mapOf("diseases" to facts)
        if (currentUser != null) {
            db.collection(collection)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Get the first document if the collection is not empty
                        val document = querySnapshot.documents.first()
                        val documentId = document.id
                        db.collection(collection).document(documentId)
                            .set(data)
                            .addOnSuccessListener {
                                Log.i(tag, "Success!!")
                            }
                    } else {
                        // Handle the case where the collection is empty
                        Log.i(tag, "No documents found in the collection.")
                        val documentId = "111"
                        db.collection(collection).document(documentId)
                            .set(data)
                            .addOnSuccessListener {
                                Log.i(tag, "Success!!")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(tag, "Error getting documents: ", exception)
                }
        }
    }

}
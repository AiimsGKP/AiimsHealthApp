package com.example.aiimshealthapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SocioDemographic : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private var firstName = ""
    private var lastName = ""
    private var gender = ""
    private var age = ""
    private var educationLevel = ""
    private var employmentStatus = ""
    private var height = ""
    private var weight = ""
    private var bmi = ""
    private var waist = ""
    private var smoking: Boolean = false
    private var alcohol: Boolean = false
    private var diabetes: String = ""
    private var hypertension: String = ""
    private var medication: String = ""
    private var sleep: String = ""
    private var diet: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_socio_demographic)


        val btnNext = findViewById<Button>(R.id.btnNext)
        btnNext.setOnClickListener {
            firstName = findViewById<EditText>(R.id.firstName).text.toString()
            lastName = findViewById<EditText>(R.id.lastName).text.toString()
            gender = findViewById<Spinner>(R.id.gender).selectedItem.toString()
            age = findViewById<EditText>(R.id.age).text.toString()
            if (firstName.isNotEmpty() && lastName.isNotEmpty() && age.isNotEmpty()) {
                setContentView(R.layout.activity_income_data)
                val btnNextNext = findViewById<Button>(R.id.btnNextNext)
                btnNextNext.setOnClickListener {
                    educationLevel = findViewById<Spinner>(R.id.etEducation).selectedItem.toString()
                    employmentStatus = findViewById<Spinner>(R.id.spinnerEmployment).selectedItem.toString()
                    if (educationLevel.isNotEmpty() && employmentStatus.isNotEmpty()){
                        setContentView(R.layout.activity_anthropometry)
                        val editTextHeight = findViewById<EditText>(R.id.etHeight)
                        val editTextWeight = findViewById<EditText>(R.id.etWeight)
                        val editTextBMI = findViewById<EditText>(R.id.etBMI)

                        val textWatcher = object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                            override fun afterTextChanged(s: Editable?) {
                                calculateAndDisplayBMI(editTextHeight, editTextWeight, editTextBMI)
                            }
                        }

                        editTextHeight.addTextChangedListener(textWatcher)
                        editTextWeight.addTextChangedListener(textWatcher)
                        val btnNextNextNext = findViewById<Button>(R.id.btnNextNextNext)
                        btnNextNextNext.setOnClickListener {
                            waist = findViewById<EditText>(R.id.etWaist).text.toString()
                            if (height.isNotEmpty() && weight.isNotEmpty() && waist.isNotEmpty()){
                                setContentView(R.layout.activity_medical)
                                val btnNextNextNextNext = findViewById<Button>(R.id.btnNextNextNextNext)
                                btnNextNextNextNext.setOnClickListener {
                                    Toast.makeText(this,  "Button Clicked", Toast.LENGTH_SHORT).show()
                                    if(findViewById<Spinner>(R.id.spTobacco).selectedItem.toString() == "Yes") smoking = true
                                    if(findViewById<Spinner>(R.id.spAlcohol).selectedItem.toString() == "Yes") alcohol = true
                                    diabetes = findViewById<EditText>(R.id.etDiabetes).text.toString()
                                    hypertension = findViewById<EditText>(R.id.etHypertension).text.toString()
                                    medication = findViewById<EditText>(R.id.etMedication).text.toString()
                                    sleep = findViewById<EditText>(R.id.etSleep).text.toString()
                                    diet = findViewById<Spinner>(R.id.spDiet).selectedItem.toString()
                                    if(findViewById<Spinner>(R.id.spDiabetes).selectedItem.toString() == "No")diabetes = "0"
                                    if(findViewById<Spinner>(R.id.spHypertension).selectedItem.toString() == "No")hypertension = "0"

                                    if (diabetes.isNotEmpty() && hypertension.isNotEmpty() && medication.isNotEmpty() && sleep.isNotEmpty() && diet.isNotEmpty()) {
                                        storeData()
                                        val intent = Intent(this, QuizPageActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private fun storeData(){

        val metricData = MetricsData(firstName, lastName, gender, age, educationLevel, employmentStatus, height, weight, bmi, waist, smoking, alcohol, diabetes, hypertension, medication, sleep, diet)
        val username = currentUser?.email.toString().substringBefore("@")
        val user = User(username ?: "Unknown User", currentUser?.email ?: "No Email")
        val userId = auth.currentUser?.uid ?: "unknown_user"
        val metrics = Metrics(user = user, metrics = metricData)

        db.collection("metrics")
            .whereEqualTo("email", currentUser?.email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && documents.isEmpty.not()) {
                    val document = documents.documents.firstOrNull()
                    if (document != null) {
                        val newData = metrics.toMap()
                        db.collection("metrics").document(userId)
                            .set(newData)
                            .addOnSuccessListener {
                                Toast.makeText(baseContext, "Data Recorded successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(baseContext, "Error Recording Data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    val newData = metrics.toMap()
                    db.collection("metrics").document(userId)
                        .set(newData)
                        .addOnSuccessListener {
                            Toast.makeText(baseContext, "Data Recorded successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(baseContext, "Error Recording Data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents.", exception)
            }
    }


    private fun calculateAndDisplayBMI(heightField: EditText, weightField: EditText, bmiField: EditText) {
        height = heightField.text.toString()
        weight = weightField.text.toString()

        if (height.isNotEmpty() && weight.isNotEmpty()) {
            val height = height.toDoubleOrNull()?.div(100)
            val weight = weight.toDoubleOrNull()

            if (height != null && weight != null && height > 0) {
                bmi = String.format("%.2f", weight / (height * height))
                bmiField.setText(bmi)
            } else {
                bmiField.setText("")
            }
        } else {
            bmiField.setText("")
        }
    }
}
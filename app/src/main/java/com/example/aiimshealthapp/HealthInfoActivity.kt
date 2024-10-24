package com.example.aiimshealthapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.aiimshealthapp.databinding.ActivityHealthInfoBinding
import com.example.aiimshealthapp.models.HealthEducationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HealthInfoActivity : AppCompatActivity(){
    lateinit var health_data: List<Map<String, Any>>
    lateinit var binding:ActivityHealthInfoBinding
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val tag = "CHECK_RESPONSE"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHealthInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ncd.setOnClickListener{
            if(binding.ncdData.isVisible){
                binding.ncdData.visibility = View.GONE
                binding.expandNCD.setImageResource(R.drawable.ic_down_arrow)
            }
            else{
                binding.ncdData.visibility = View.VISIBLE
                binding.expandNCD.setImageResource(R.drawable.ic_up_arrow)
            }
        }
        val clickListener = View.OnClickListener { view ->
            // Get the button's id and send the appropriate data
            when (view.id) {
                R.id.diabetes -> sendDataToNextActivity(findItemByName(health_data, "Diabetes"))
                R.id.hypertension -> sendDataToNextActivity(findItemByName(health_data, "Hypertension"))
                R.id.cancer -> sendDataToNextActivity(findItemByName(health_data, "Cancer"))
                R.id.copd -> sendDataToNextActivity(findItemByName(health_data, "COPD"))
            }
        }

        binding.diabetes.setOnClickListener(clickListener)
        binding.hypertension.setOnClickListener(clickListener)
        binding.cancer.setOnClickListener(clickListener)
        binding.copd.setOnClickListener(clickListener)

        loadData(this, object : DataCallback {
            override fun onDataLoaded(data: List<Map<String, Any>>?) {
                // Handle the returned data
                if (data != null) {
                    health_data = data
                } else {
                    Log.i(tag,"No data")
                }
            }
        })

    }

    fun findItemByName(data: List<Map<String, Any>>, nameToFind: String): Map<String, Any>? {
        for (item in data) {
            if (item["name"] == nameToFind) {
                return item
            }
        }
        return null
    }


    interface DataCallback {
        fun onDataLoaded(data: List<Map<String, Any>>?)
    }

    private fun loadData(context: Context, callback: DataCallback) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("HealthPrefs", Context.MODE_PRIVATE)
        val gson = Gson()

        // Check SharedPreferences for existing data
        val jsonData = sharedPreferences.getString("health_education_facts", null)
        if (jsonData != null && jsonData.isNotEmpty()) {
            // Data exists in SharedPreferences, try to parse it
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val dataFromPrefs: List<Map<String, Any>>? = gson.fromJson(jsonData, type)
            Log.i(tag, dataFromPrefs.toString())
            if (dataFromPrefs != null) {
                // Pass the data back via callback
                callback.onDataLoaded(dataFromPrefs)
            } else {
                // Handle case where fromJson returned null due to malformed data
                callback.onDataLoaded(null)
            }

        } else {
            // If data doesn't exist in SharedPreferences, fetch from Firestore
            if (currentUser != null) {
                db.collection("health_education")
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents != null) {
                            var data: List<Map<String, Any>>? = null
                            for (document in documents) {
                                data = document.data["diseases"] as? List<Map<String, Any>>
                                if (data != null) {
                                    // Store the fetched data in SharedPreferences
                                    val jsonDataToStore = gson.toJson(data)
                                    val editor = sharedPreferences.edit()
                                    editor.putString("health_education_facts", jsonDataToStore)
                                    editor.apply()
                                }
                            }
                            // Pass the fetched data back via callback
                            callback.onDataLoaded(data)
                        } else {
                            // If no documents found, return null or empty data
                            callback.onDataLoaded(null)
                        }
                    }
                    .addOnFailureListener {
                        // Handle failure, e.g., return null or handle the error
                        callback.onDataLoaded(null)
                    }
            } else {
                // If no current user, return null
                callback.onDataLoaded(null)
            }
        }
    }

    private fun sendDataToNextActivity(map: Map<String, Any>?) {
        val intent = Intent(this, HealthInfoPageActivity::class.java)
        val bundle = Bundle()

        if (map != null) {
            for ((key, value) in map) {
                when (value) {
                    is String -> bundle.putString(key, value)
                    is Int -> bundle.putInt(key, value)
                    is Boolean -> bundle.putBoolean(key, value)
                    is Float -> bundle.putFloat(key, value)
                }
            }
        }

        intent.putExtras(bundle)
        startActivity(intent)
    }

}
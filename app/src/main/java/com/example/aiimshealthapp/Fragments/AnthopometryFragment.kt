package com.example.aiimshealthapp.Fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.databinding.FragmentAnthopometryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AnthopometryFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private var height = ""
    private var weight = ""
    private var bmi = ""
    private var waist = ""
    private var _binding: FragmentAnthopometryBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val editable = Editable.Factory.getInstance()
        _binding = FragmentAnthopometryBinding.inflate(inflater, container, false)
        binding.btnNext.setOnClickListener{
            val feet = binding.feet.text.toString().trim()
            val inch = binding.inch.text.toString().trim()
            weight = binding.weight.text.toString().trim()
            waist = binding.waist.text.toString().trim()
            if( feet.isEmpty()){
                binding.feet.error = "Feet Value is required"
            }
            if( inch.isEmpty()){
                binding.inch.error = "Inch value is required"
            }
            if( weight.isEmpty()){
                binding.weight.error = "Weight is required"
            }
            if( waist.isEmpty()){
                binding.waist.error = "Waist Circumference is required"
            }
            if(feet.isNotEmpty() && inch.isNotEmpty() && weight.isNotEmpty() && waist.isNotEmpty()){

                editor.putString("feet", feet)
                editor.putString("inch", inch)
                editor.putString("weight", weight)
                editor.putString("waist", waist)
                editor.apply()

                val cm_from_feet = feet.toInt() * 30.48
                val cm_from_inches = inch.toInt() * 2.54
                height = (cm_from_feet + cm_from_inches).toString()
                bmi = (weight.toDouble() / ((height.toDouble() / 100) * (height.toDouble() / 100))).toString()


                if (currentUser != null) {
                    db.collection("metrics")
                        .whereEqualTo("user.email", currentUser.email)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents != null) {
                                for (document in documents) {
                                    val documentId = document.id
                                    val data = document.data
                                    val metrics = (data["metrics"] as? Map<*, *>)?.toMutableMap()
                                    metrics?.set("height", height)
                                    metrics?.set("weight", weight)
                                    metrics?.set("waist", waist)
                                    metrics?.set("bmi", bmi)

                                    // Update the document with the new metrics map
                                    val updates = mapOf("metrics" to metrics)
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
                        }
                }

                //redirect
                val secondFragment = EmploymentEducationFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout2, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                    .addToBackStack(null) // Optional: Adds the transaction to the back stack so the user can navigate back
                    .commit()
            }
        }

        binding.backBtn.setOnClickListener{
            val secondFragment = SocioDemographicFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout2, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                .commit()

        }

        binding.feet.text = editable.newEditable(sharedPreferences.getString("feet", ""))
        binding.inch.text = editable.newEditable(sharedPreferences.getString("inch", ""))
        binding.weight.text = editable.newEditable(sharedPreferences.getString("weight", ""))
        binding.waist.text = editable.newEditable(sharedPreferences.getString("waist", ""))
        return binding.root
    }


}
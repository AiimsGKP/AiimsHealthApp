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
import com.example.aiimshealthapp.databinding.FragmentEmploymentEducationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EmploymentEducationFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    var educationLevel = ""
    var employmentStatus = ""
    private var _binding: FragmentEmploymentEducationBinding? = null
    private val binding get() = _binding!!
    val custom_tag = "CHECK_RESPONSE"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val editable = Editable.Factory.getInstance()
        _binding = FragmentEmploymentEducationBinding.inflate(inflater, container, false)

        binding.employed.setOnClickListener{
            binding.employed.setBackgroundResource(R.drawable.rounded_corner)
            binding.employed.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.notEmployed.setBackgroundResource(R.drawable.not_selected)
            binding.notEmployed.setTextColor(getResources().getColor(R.color.neutral_light_1))
            employmentStatus = "employed"
        }
        binding.notEmployed.setOnClickListener{
            binding.notEmployed.setBackgroundResource(R.drawable.rounded_corner)
            binding.notEmployed.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.employed.setBackgroundResource(R.drawable.not_selected)
            binding.employed.setTextColor(getResources().getColor(R.color.neutral_light_1))
            employmentStatus = "not_employed"
        }
        binding.backBtn.setOnClickListener{
            val secondFragment = AnthopometryFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout2, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                .commit()
        }
        binding.btnNext.setOnClickListener {
            if(employmentStatus == "employed") editor.putBoolean("employmentStatus", true)
            else editor.putBoolean("employmentStatus", false)
            educationLevel = binding.mySpinner.selectedItem.toString()
            when(educationLevel){
                "Primary School" -> editor.putInt("educationLevel",0)
                "Middle School" -> editor.putInt("educationLevel",1)
                "High School" -> editor.putInt("educationLevel",2)
                "Intermediate/Diploma" -> editor.putInt("educationLevel",3)
                "Graduate" -> editor.putInt("educationLevel",4)
                "Professional Degree" -> editor.putInt("educationLevel",5)
                "Other" -> editor.putInt("educationLevel",6)
                else -> editor.putInt("educationLevel",-1)
            }

            editor.apply()
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

                                Log.i(custom_tag, "${employmentStatus} ${educationLevel}")
                                metrics?.set("employmentStatus", employmentStatus)
                                metrics?.set("educationLevel", educationLevel)

                                val updates = mapOf("metrics" to metrics)
                                db.collection("metrics").document(documentId)
                                    .update(updates)
                                    .addOnSuccessListener {
                                        Log.d(custom_tag, "Metrics updated successfully.")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(custom_tag, "Error updating metrics", e)
                                    }
                            }
                        }
                    }
            }
            val secondFragment = HealthHistoryFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout2, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                .addToBackStack(null) // Optional: Adds the transaction to the back stack so the user can navigate back
                .commit()
        }
        if(sharedPreferences.getBoolean("employmentStatus", true)){
            binding.employed.setBackgroundResource(R.drawable.rounded_corner)
            binding.employed.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.notEmployed.setBackgroundResource(R.drawable.not_selected)
            binding.notEmployed.setTextColor(getResources().getColor(R.color.neutral_light_1))
            employmentStatus = "employed"
        }
        else{
            binding.notEmployed.setBackgroundResource(R.drawable.rounded_corner)
            binding.notEmployed.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.employed.setBackgroundResource(R.drawable.not_selected)
            binding.employed.setTextColor(getResources().getColor(R.color.neutral_light_1))
            employmentStatus = "not_employed"
        }
        binding.mySpinner.setSelection(sharedPreferences.getInt("educationLevel",0))

        return binding.root
    }

}
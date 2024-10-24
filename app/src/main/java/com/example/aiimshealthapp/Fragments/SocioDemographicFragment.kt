package com.example.aiimshealthapp.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aiimshealthapp.Dashboard
import com.example.aiimshealthapp.LoginWithUsername
import com.example.aiimshealthapp.MainActivity
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.databinding.FragmentSocioDemographicBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SocioDemographicFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private var firstName = ""
    private var lastName = ""
    private var gender = "male"
    private var age = ""

        private var _binding: FragmentSocioDemographicBinding? = null
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
        _binding = FragmentSocioDemographicBinding.inflate(inflater, container, false)

        binding.female.setOnClickListener{
            binding.female.setBackgroundResource(R.drawable.rounded_corner)
            binding.female.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.male.setBackgroundResource(R.drawable.not_selected)
            binding.male.setTextColor(getResources().getColor(R.color.neutral_light_1))
            gender = "female"
        }
        binding.male.setOnClickListener{
            binding.male.setBackgroundResource(R.drawable.rounded_corner)
            binding.male.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.female.setBackgroundResource(R.drawable.not_selected)
            binding.female.setTextColor(getResources().getColor(R.color.neutral_light_1))
            gender = "male"
        }
        binding.backBtn.setOnClickListener{
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        }
        binding.btnNext.setOnClickListener{
            firstName = binding.firstName.text.toString().trim()
            lastName = binding.lastName.text.toString().trim()
            age = binding.age.text.toString().trim()
            if( firstName.isEmpty()){
                binding.firstName.error = "First Name is required"
            }
            if( lastName.isEmpty()){
                binding.lastName.error = "Last Name is required"
            }
            if( age.isEmpty()){
                binding.age.error = "Age is required"
            }
            if(firstName.isNotEmpty() && lastName.isNotEmpty() && age.isNotEmpty()){
                editor.putString("firstName", firstName)
                editor.putString("lastName", lastName)
                editor.putString("age", age)
                if(gender == "male") editor.putBoolean("gender", true)
                else editor.putBoolean("gender", false)

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
                                    metrics?.set("firstName", firstName)
                                    metrics?.set("lastName", lastName)
                                    metrics?.set("age", age)
                                    metrics?.set("gender", gender)

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
                val secondFragment = AnthopometryFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout2, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                    .addToBackStack(null) // Optional: Adds the transaction to the back stack so the user can navigate back
                    .commit()
            }
        }


        binding.firstName.text = editable.newEditable(sharedPreferences.getString("firstName", ""))
        binding.lastName.text = editable.newEditable(sharedPreferences.getString("lastName", ""))
        binding.age.text = editable.newEditable(sharedPreferences.getString("age", ""))
        if(sharedPreferences.getBoolean("gender", true)){
            binding.male.setBackgroundResource(R.drawable.rounded_corner)
            binding.male.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.female.setBackgroundResource(R.drawable.not_selected)
            binding.female.setTextColor(getResources().getColor(R.color.neutral_light_1))
            gender = "male"
        }
        else{
            binding.male.setBackgroundResource(R.drawable.rounded_corner)
            binding.male.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.female.setBackgroundResource(R.drawable.rounded_corner_2)
            binding.female.setTextColor(getResources().getColor(R.color.neutral_light_5))
            gender = "female"
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the binding reference to avoid memory leaks
        _binding = null
    }
}
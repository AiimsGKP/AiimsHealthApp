package com.example.aiimshealthapp.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.aiimshealthapp.LoginWithUsername
import com.example.aiimshealthapp.QuizPageActivity
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.databinding.FragmentHealthHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HealthHistoryFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private var smoking: Boolean = false
    private var alcohol: Boolean = false
    private var diabetes: String = ""
    private var hypertension: String = ""
    private var medication: String = ""
    private var sleep: String = ""
    private var diet: String = ""
    private var enabledDiabetes = false
    private var enabledHypertension = false
    val custom_tag = "CHECK_RESPONSE"
    private var _binding: FragmentHealthHistoryBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthHistoryBinding.inflate(inflater, container, false)
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val editable = Editable.Factory.getInstance()

        setupRadioGroupListeners()

        binding.vegetarian.setOnClickListener{
            binding.vegetarian.setBackgroundResource(R.drawable.rounded_corner)
            binding.vegetarian.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.nonvegetarian.setBackgroundResource(R.drawable.not_selected)
            binding.nonvegetarian.setTextColor(getResources().getColor(R.color.neutral_light_1))
            diet = "vegetarian"
        }
        binding.nonvegetarian.setOnClickListener{
            binding.nonvegetarian.setBackgroundResource(R.drawable.rounded_corner)
            binding.nonvegetarian.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.vegetarian.setBackgroundResource(R.drawable.not_selected)
            binding.vegetarian.setTextColor(getResources().getColor(R.color.neutral_light_1))
            diet = "non_vegetarian"
        }
        binding.btnNext.setOnClickListener {
            val radioTobaccoId = binding.radioTobacco.checkedRadioButtonId
            val radioAlcoholId = binding.radioAlcohol.checkedRadioButtonId
            val radioDiabetesId = binding.radioDiabetes.checkedRadioButtonId
            val radioHypertensionId = binding.radioHypertension.checkedRadioButtonId

            //check for blank input
            if(binding.radioTobacco.checkedRadioButtonId == -1) Toast.makeText(
                requireContext(),
                "Please select an option for Tobacco Consumption",
                Toast.LENGTH_SHORT
            ).show()
            else if(binding.radioAlcohol.checkedRadioButtonId == -1) Toast.makeText(
                requireContext(),
                "Please select an option for Alcohol Consumption",
                Toast.LENGTH_SHORT
            ).show()
            else if(binding.radioDiabetes.checkedRadioButtonId == -1) Toast.makeText(
                requireContext(),
                "Please select an option for Diabetes History",
                Toast.LENGTH_SHORT
            ).show()
            else if(binding.radioHypertension.checkedRadioButtonId == -1) Toast.makeText(
                requireContext(),
                "Please select an option for Hypertension History",
                Toast.LENGTH_SHORT
            ).show()
            else if( binding.etSleep.text.trim().isEmpty()){
                binding.etSleep.error = "Sleep hours are required"
            }
            else{
                if (binding.yesDiabetes.isChecked) {
                    if(binding.etDiabetes.text.isEmpty()) {
                        binding.etDiabetes.error = "No. of years is required"
                        enabledDiabetes = false
                    }
                    else{
                        enabledDiabetes = true
                    }
                }
                else if(binding.noDiabetes.isChecked){enabledDiabetes = true}
                if (binding.yesHypertension.isChecked) {
                    if(binding.etHypertension.text.isEmpty()) {
                        binding.etHypertension.error = "No. of years is required"
                        enabledHypertension = false
                    }
                    else{
                        enabledHypertension = true
                    }
                }
                else if(binding.noHypertension.isChecked){enabledHypertension = true}
                if(enabledDiabetes && enabledHypertension){
                    //fetching the data
                    smoking = binding.radioTobacco.findViewById<RadioButton>(radioTobaccoId).text == "Yes"
                    alcohol = binding.radioAlcohol.findViewById<RadioButton>(radioAlcoholId).text == "Yes"

                    if(binding.lldiabetes.isVisible) diabetes = binding.etDiabetes.text.toString() else diabetes = ""
                    if(binding.llhypertension.isVisible) hypertension = binding.etHypertension.text.toString() else hypertension = ""
                    medication = binding.etMedication.text.toString()
                    sleep = binding.etSleep.text.toString()


                    //storing in the shared preferences
                    if(diet == "vegetarian") editor.putBoolean("diet", true)
                    else editor.putBoolean("diet", false)
                    editor.putString("smoking", binding.radioTobacco.findViewById<RadioButton>(radioTobaccoId).text.toString())
                    editor.putString("alcohol", binding.radioAlcohol.findViewById<RadioButton>(radioAlcoholId).text.toString())
                    editor.putString("r_diabetes", binding.radioDiabetes.findViewById<RadioButton>(radioDiabetesId).text.toString())
                    editor.putString("r_hypertension", binding.radioHypertension.findViewById<RadioButton>(radioHypertensionId).text.toString())
                    editor.putString("diabetes", diabetes)
                    editor.putString("hypertension", hypertension)
                    editor.putString("medication", medication)
                    editor.putString("sleep", sleep)
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

                                        metrics?.set("smoking", smoking)
                                        metrics?.set("alcohol", alcohol)
                                        metrics?.set("diabetes", diabetes)
                                        metrics?.set("hypertension", hypertension)
                                        metrics?.set("medication", medication)
                                        metrics?.set("sleep", sleep)
                                        metrics?.set("diet", diet)

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
                    val intent = Intent(requireContext(), QuizPageActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }

            }
        }

        binding.backBtn.setOnClickListener{
            val secondFragment = EmploymentEducationFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout2, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                .commit()
        }

        selectRadioButtonBasedOnText(binding.radioTobacco, sharedPreferences.getString("smoking", "").toString())
        selectRadioButtonBasedOnText(binding.radioAlcohol, sharedPreferences.getString("alcohol", "").toString())
        selectRadioButtonBasedOnText(binding.radioDiabetes, sharedPreferences.getString("r_diabetes", "").toString())
        selectRadioButtonBasedOnText(binding.radioHypertension, sharedPreferences.getString("r_hypertension", "").toString())
        binding.etDiabetes.text = editable.newEditable(sharedPreferences.getString("diabetes", ""))
        binding.etHypertension.text = editable.newEditable(sharedPreferences.getString("hypertension", ""))
        binding.etMedication.text = editable.newEditable(sharedPreferences.getString("medication", ""))
        binding.etSleep.text = editable.newEditable(sharedPreferences.getString("sleep", ""))
        if(sharedPreferences.getBoolean("diet", true)){
            binding.vegetarian.setBackgroundResource(R.drawable.rounded_corner)
            binding.vegetarian.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.nonvegetarian.setBackgroundResource(R.drawable.not_selected)
            binding.nonvegetarian.setTextColor(getResources().getColor(R.color.neutral_light_1))
            diet = "vegetarian"
        }
        else{
            binding.nonvegetarian.setBackgroundResource(R.drawable.rounded_corner)
            binding.nonvegetarian.setTextColor(getResources().getColor(R.color.highlight_2))
            binding.vegetarian.setBackgroundResource(R.drawable.not_selected)
            binding.vegetarian.setTextColor(getResources().getColor(R.color.neutral_light_1))
            diet = "non_vegetarian"
        }
        return binding.root
    }
    private fun setupRadioGroupListeners() {
        // Diabetes RadioGroup listener
        binding.radioDiabetes.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            if (selectedRadioButton?.text == "Yes") {
                binding.lldiabetes.visibility = View.VISIBLE
            } else {
                // Hide the years of diabetes layout
                binding.lldiabetes.visibility = View.GONE
            }
        }

        // Hypertension RadioGroup listener
        binding.radioHypertension.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            if (selectedRadioButton?.text == "Yes") {
                // Show the years of hypertension layout
                binding.llhypertension.visibility = View.VISIBLE
            } else {
                // Hide the years of hypertension layout
                binding.llhypertension.visibility = View.GONE
            }
        }
    }
    fun selectRadioButtonBasedOnText(radioGroup: RadioGroup, selectedText: String) {
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as RadioButton
            if (radioButton.text == selectedText) {
                radioButton.isChecked = true
                break
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference
    }
}
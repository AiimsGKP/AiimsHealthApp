package com.example.aiimshealthapp.Fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.databinding.FragmentFoodDetailsBinding
import com.example.aiimshealthapp.models.FoodData
import com.example.aiimshealthapp.models.NutrientDetail

class FoodDetailsFragment : Fragment() {
    private var _binding: FragmentFoodDetailsBinding? = null
    private val binding get() = _binding!!
    private val tag = "CHECK_RESPONSE"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFoodDetailsBinding.inflate(inflater, container, false)
        val foodData = arguments?.getParcelable<FoodData>("food_data_key")
        val nutriScoreDescription = mapOf(
            "A" to "This product has excellent nutritional value.",
            "B" to "This product has good nutritional value.",
            "C" to "This product has moderate nutritional value.",
            "D" to "This product has poor nutritional value.",
            "E" to "This product has very bad nutritional value."
        )
        val nutriScoreColors = mapOf(
            "A" to "#00B050",  // Green
            "B" to "#92D050",  // Light Green
            "C" to "#FFD700",  // Yellow
            "D" to "#FFA500",  // Orange
            "E" to "#FF0000"   // Red
        )



        foodData?.let {
            // Use safe-call operator and default values to avoid NullPointerException
            binding.title.text = it.product.product_name ?: ""
            binding.quantitiy.text = it.product.quantity ?: ""
            binding.barcode.text = it.code ?: ""
            binding.brand.text = it.product.brands ?: ""

            // Ensure the list is not empty before accessing the first element
            binding.countryOrigin.text = if (it.product.countries_tags.isNotEmpty()) {
                it.product.countries_tags[0]
            } else {
                "No country of origin available"
            }

            binding.ingredients.text = if (!it.product.ingredients_text.isNullOrEmpty()) {
                it.product.ingredients_text
            } else {
                "No ingredients data found"
            }

            binding.energy.text = "${it.product.nutriments.energyKcal.toInt()} kcal"
            binding.carbs.text = "${String.format("%.1f", it.product.nutriments.carbohydrates).toDouble()} ${it.product.nutriments.carbohydrates_unit}"
            binding.fat.text = "${String.format("%.1f", it.product.nutriments.fat).toDouble()} ${it.product.nutriments.fat_unit}"
            binding.protein.text = "${String.format("%.1f", it.product.nutriments.proteins).toDouble()} ${it.product.nutriments.proteins_unit}"
            binding.salt.text = "${String.format("%.1f", it.product.nutriments.salt).toDouble()} ${it.product.nutriments.salt_unit}"
            binding.sodium.text = "${String.format("%.1f", it.product.nutriments.sodium).toDouble()} ${it.product.nutriments.sodium_unit}"
            binding.sugar.text = "${String.format("%.1f", it.product.nutriments.sugars).toDouble()} ${it.product.nutriments.sugars_unit}"

            val nutriscoreGrade = it.product.nutriscore.`2023`.grade.uppercase()
            binding.nutriTag.text = "Nutri Score $nutriscoreGrade"
            binding.nutriTag.setTextColor(Color.parseColor(nutriScoreColors[nutriscoreGrade] ?: "#C5C6CC"))
            binding.nutriScoreDescription.text = nutriScoreDescription[nutriscoreGrade] ?: "No description available"
            binding.positives.text = getNutrientsData(it.product.nutriscore.`2023`.data.components.positive)
            binding.negatives.text = getNutrientsData(it.product.nutriscore.`2023`.data.components.negative)

            if (it.product.nova_group_debug == ""){
                binding.novaTag.text = convertText(it.product.nova_groups_tags[0])
            }
            else{
                binding.novaTag.text = "Missing Ingredients"
            }

            Glide.with(this)
                .load(it.product.image_front_url)
                .into(binding.frontImage)
        }


        binding.expandNutri.setOnClickListener{
            if(binding.nutriData.isVisible){
                binding.nutriData.visibility = View.GONE
                binding.expandNutri.setImageResource(R.drawable.ic_down_arrow)
            }
            else{
                binding.nutriData.visibility = View.VISIBLE
                binding.expandNutri.setImageResource(R.drawable.ic_up_arrow)
            }
        }

        return binding.root
    }

    private fun getNutrientsData(data: List<NutrientDetail>):String{
        var result = ""
        for(i in data){
           result += "${formatString(i.id)} \n"
        }
        return result
    }
    private fun formatString(input: String): String {
        return input.split("_")
            .joinToString(" ") { it.capitalize() }
    }
    private fun convertText(input: String): String {
        // Step 1: Remove the prefix before the first dash (i.e., "en:4-")
        val cleanedInput = input.substringAfter("-")

        // Step 2: Replace hyphens with spaces
        val withSpaces = cleanedInput.replace("-", " ")

        // Step 3: Capitalize the first letter of the string
        return withSpaces.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

}
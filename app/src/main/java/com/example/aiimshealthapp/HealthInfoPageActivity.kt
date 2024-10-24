package com.example.aiimshealthapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.aiimshealthapp.databinding.ActivityHealthInfoBinding
import com.example.aiimshealthapp.databinding.ActivityHealthInfoPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

class HealthInfoPageActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val collection = "fact_collection"
    private val tag = "CHECK_RESPONSE"
    private lateinit var progressBar : ProgressBar
    lateinit var binding:ActivityHealthInfoPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHealthInfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
//        setLoading(true, progressBar)
        loadListeners()
        val receivedMap = intent.getSerializableExtra("myMap") as? HashMap<String, Serializable>
        if (receivedMap != null) {
            Log.i("TargetActivity", "Received Map: $receivedMap")
        }
    }

    private fun loadListeners() {
        binding.definition.setOnClickListener{
            if(binding.definitionContent.isVisible){
                binding.definitionContent.visibility = View.GONE
                binding.expandDef.setImageResource(R.drawable.ic_down_arrow)
            }
            else{
                binding.definitionContent.visibility = View.VISIBLE
                binding.expandDef.setImageResource(R.drawable.ic_up_arrow)
            }
        }
        binding.causes.setOnClickListener{
            if(binding.causesContent.isVisible){
                binding.causesContent.visibility = View.GONE
                binding.expandCauses.setImageResource(R.drawable.ic_down_arrow)
            }
            else{
                binding.causesContent.visibility = View.VISIBLE
                binding.expandCauses.setImageResource(R.drawable.ic_up_arrow)
            }
        }
        binding.prevention.setOnClickListener{
            if(binding.preventionContent.isVisible){
                binding.preventionContent.visibility = View.GONE
                binding.expandPrevention.setImageResource(R.drawable.ic_down_arrow)
            }
            else{
                binding.preventionContent.visibility = View.VISIBLE
                binding.expandPrevention.setImageResource(R.drawable.ic_up_arrow)
            }
        }
        binding.symptoms.setOnClickListener{
            if(binding.symptomsContent.isVisible){
                binding.symptomsContent.visibility = View.GONE
                binding.expandSymptoms.setImageResource(R.drawable.ic_down_arrow)
            }
            else{
                binding.symptomsContent.visibility = View.VISIBLE
                binding.expandSymptoms.setImageResource(R.drawable.ic_up_arrow)
            }
        }
        binding.whatToEat.setOnClickListener{
            if(binding.whatToEatContent.isVisible){
                binding.whatToEatContent.visibility = View.GONE
                binding.expandWhatToEat.setImageResource(R.drawable.ic_down_arrow)
            }
            else{
                binding.whatToEatContent.visibility = View.VISIBLE
                binding.expandWhatToEat.setImageResource(R.drawable.ic_up_arrow)
            }
        }
    }

    fun setLoading(loading: Boolean, progressBar: ProgressBar) {
        if (loading) {
            progressBar.visibility = View.VISIBLE // Show the ProgressBar
        } else {
            progressBar.visibility = View.GONE // Hide the ProgressBar
        }
    }
}
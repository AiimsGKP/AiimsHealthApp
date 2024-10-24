package com.example.aiimshealthapp.Fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.aiimshealthapp.FullCircleProgressBar
import com.example.aiimshealthapp.HealthInfoActivity
import com.example.aiimshealthapp.HydrationReminderActivity
import com.example.aiimshealthapp.LoginWithUsername
import com.example.aiimshealthapp.MedicationReminderActivity
import com.example.aiimshealthapp.QuizPageActivity
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.StepCounterActivity
import com.example.aiimshealthapp.databinding.FragmentDashboardBinding
import com.example.aiimshealthapp.databinding.FragmentStepTrackerBinding
import com.example.aiimshealthapp.viewmodels.SharedViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import kotlin.random.Random

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    // TODO: Rename and change types of parameters
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private lateinit var sharedPreferences: SharedPreferences
    private val tag = "CHECK_RESPONSE"
    private val currentDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("stepCounterPrefs", Context.MODE_PRIVATE)


        sharedPreferences = requireContext().getSharedPreferences("stepCounterPrefs", Context.MODE_PRIVATE)
        setLoading(true, binding.progressBar1)
        binding.llSpeedometer.visibility = View.GONE
        binding.speedometerAlert.visibility = View.GONE
        updateProgressBars()

        setLoading(true, binding.progressBar2)
        binding.tvFact.visibility = View.GONE
        binding.tvSource.visibility = View.GONE
        binding.tvTopic.visibility = View.GONE
        getFactList { (facts, sources, topics) ->
            setLoading(false, binding.progressBar2)
            binding.tvFact.visibility = View.VISIBLE
            binding.tvSource.visibility = View.VISIBLE
            binding.tvTopic.visibility = View.VISIBLE
            val handler = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    if (facts.isNotEmpty() && sources.isNotEmpty() && topics.isNotEmpty()) {
                        val index = Random.nextInt(0, facts.size)
                        binding.tvFact.text = facts[index]
                        binding.tvSource.text = sources[index]
                        binding.tvTopic.text = topics[index]
                    } else {
                        // Handle the case where the lists are empty
                        Log.e("DashboardFragment", "One or more lists are empty!")
                        binding.tvFact.text = "No facts available"
                        binding.tvSource.text = "No source available"
                        binding.tvTopic.text = "No topic available"
                    }
                    handler.postDelayed(this, 30000)
                }
            }
            handler.post(runnable)
        }

        binding.takeTestBtn.setOnClickListener {
            val intent = Intent(requireContext(), QuizPageActivity::class.java)
            startActivity(intent)
        }

        binding.signout.setOnClickListener{
            Firebase.auth.signOut()
            val intent = Intent(requireContext(), LoginWithUsername::class.java)
            startActivity(intent)
        }
        binding.hydrationbtn.setOnClickListener(){
            val intent = Intent(requireContext(), HydrationReminderActivity::class.java)
            startActivity(intent)
        }
        binding.medicationbtn.setOnClickListener(){
            val intent = Intent(requireContext(), MedicationReminderActivity::class.java)
            startActivity(intent)
        }
        binding.tvFact.setOnClickListener {
            val intent = Intent(requireContext(), HealthInfoActivity::class.java)
            startActivity(intent)
        }
        binding.stepCounterBtn.setOnClickListener {
            val intent = Intent(requireContext(), StepCounterActivity::class.java)
            startActivity(intent)
        }

        updateReminders()
        updateSteps()

    }


    override fun onResume() {
        super.onResume()
        updateReminders()
        updateSteps()
    }

    private fun updateSteps() {
        val sharedPreferences = requireContext().getSharedPreferences("StepsPrefs", Context.MODE_PRIVATE)
        binding.stepCounterTextView.text = sharedPreferences.getInt(currentDate.toString(), 0).toString()
        binding.tvStepGoal.text = "/${sharedPreferences.getInt("stepGoal", 3000)}"
        binding.fullCircleProgressBar4.setMax(sharedPreferences.getInt("stepGoal", 3000))
        binding.fullCircleProgressBar4.setProgress(sharedPreferences.getInt(currentDate.toString(), 0))
        if(sharedPreferences.contains(currentDate.toString())) {
            binding.tvCalories.text = "${sharedPreferences.getInt("calories", 0)} cal"
        }
        else{
            binding.tvCalories.text = "0 cal"
        }
    }


    private fun updateReminders() {
        val sharedPreferences = requireContext().getSharedPreferences("reminders", Context.MODE_PRIVATE)
        binding.hydrationReminder.isChecked = sharedPreferences.getBoolean("hydration", true)
        binding.medicationReminder.isChecked = sharedPreferences.getBoolean("medication", true)
        binding.hydrationReminder.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("hydration", isChecked).apply()
        }
        binding.medicationReminder.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("medication", isChecked).apply()
        }
    }

    private fun getFactList(onFactsLoaded: (Triple<List<String>, List<String>, List<String>>) -> Unit) {
        val factsList = mutableListOf<String>()
        val sourceList = mutableListOf<String>()
        val topicList = mutableListOf<String>()
        var metrics: Map<String, Any> = emptyMap()

        val fullName = view?.findViewById<TextView>(R.id.fullName)
        val username = view?.findViewById<TextView>(R.id.username)

        if (currentUser != null) {
            val metricsTask = db.collection("metrics")
                .whereEqualTo("user.email", currentUser.email)
                .get()

            val factsTask = db.collection("fact_collection").get()

            Tasks.whenAllSuccess<QuerySnapshot>(metricsTask, factsTask)
                .addOnSuccessListener { results ->
                    val metricsDocuments = results[0] as QuerySnapshot
                    if (!metricsDocuments.isEmpty) {
                        val document = metricsDocuments.documents[0]
                        val data = document.data ?: emptyMap()
                        metrics = (data["metrics"] as? Map<String, Any>)?.toMap() ?: emptyMap()
                        fullName?.text = "${metrics["firstName"]} ${metrics["lastName"]}"
                        username?.text = currentUser.email.toString().split("@")[0]
                    }

                    val factsDocuments = results[1] as QuerySnapshot
                    for (document in factsDocuments) {
                        val data = document.data
                        val facts = data["facts"] as? List<Map<String, Any>>
                        if (facts != null) {
                            for (fact in facts) {
                                val skipFact = when {
                                    metrics["smoking"] == false && fact["topic"].toString() == "Smoking" -> true
                                    metrics["alcohol"] == false && fact["topic"].toString() == "Alcohol" -> true
                                    metrics["diabetes"] == "0" && fact["topic"].toString() == "Diabetes" -> true
                                    metrics["hypertension"] == "0" && fact["topic"].toString() == "Hypertension" -> true
                                    else -> false
                                }

                                if (!skipFact) {
                                    factsList.add(fact["fact"].toString())
                                    sourceList.add(fact["source"].toString())
                                    topicList.add(fact["topic"].toString())
                                }
                            }
                        }
                    }

                    onFactsLoaded(Triple(factsList, sourceList, topicList))
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Error fetching data: ", e)
                    onFactsLoaded(Triple(emptyList(), emptyList(), emptyList()))
                }
        } else {
            onFactsLoaded(Triple(emptyList(), emptyList(), emptyList()))
        }
    }

    private fun updateProgressBars() {
        var count = 0
        val scores = mutableListOf<Int>()
        val sharedPreferences = context?.getSharedPreferences("progressBarPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        // Function to load the stored progress from SharedPreferences
        fun loadProgressFromPreferences() {
            val physicalScore = sharedPreferences?.getInt("physicalScore", 0) ?: 0
            val mentalScore = sharedPreferences?.getInt("mentalScore", 0) ?: 0
            val socialScore = sharedPreferences?.getInt("socialScore", 0) ?: 0

            view?.findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar1)?.apply {
                visibility = View.VISIBLE
                setProgress(physicalScore)
            }
            view?.findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar2)?.apply {
                visibility = View.VISIBLE
                setProgress(mentalScore)
            }
            view?.findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar3)?.apply {
                visibility = View.VISIBLE
                setProgress(socialScore)
            }

            view?.findViewById<TextView>(R.id.physicalScore)?.text = "Physical:\n$physicalScore%"
            view?.findViewById<TextView>(R.id.mentalScore)?.text = "Mental:\n$mentalScore%"
            view?.findViewById<TextView>(R.id.socialScore)?.text = "Social:\n$socialScore%"
        }

        if (currentUser != null) {
            db.collection("questions2")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // Load progress from SharedPreferences if no documents are found
                        loadProgressFromPreferences()
                        setLoading(false, binding.progressBar1)
                        binding.speedometerAlert.visibility = View.VISIBLE
                        binding.llSpeedometer.visibility = View.GONE

                    } else {
                        for (document in documents) {
                            val data = document.data
                            val quizes = data["quizes"] as? List<Map<String, Any>>

                            if (quizes != null) {
                                for (quiz in quizes) {
                                    val score = (quiz["score"] as? Number)?.toInt() ?: 0
                                    scores.add(score)
                                    val answers = quiz["answers"] as? List<String>
                                    if (!answers.isNullOrEmpty()) count += 1
                                    if (count == 3) {
                                        if (scores.size >= 3) {
                                            val physicalScore = ((scores[0].toDouble() / 600) * 100).coerceIn(0.0, 100.0).toInt()
                                            val mentalScore = (((scores[1] - 7).toDouble() / (35 - 7)) * 100).coerceIn(0.0, 100.0).toInt()
                                            val socialScore = (100 - ((scores[2].toDouble() / 6) * 100)).coerceIn(0.0, 100.0).toInt()

                                            // Store progress in SharedPreferences
                                            editor?.putInt("physicalScore", physicalScore)
                                            editor?.putInt("mentalScore", mentalScore)
                                            editor?.putInt("socialScore", socialScore)
                                            editor?.apply()

                                            setLoading(false, binding.progressBar1)
                                            binding.llSpeedometer.visibility = View.VISIBLE
                                            binding.speedometerAlert.visibility = View.GONE

                                            view?.findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar1)?.apply {
                                                visibility = View.VISIBLE
                                                setProgress(physicalScore)
                                            }
                                            view?.findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar2)?.apply {
                                                visibility = View.VISIBLE
                                                setProgress(mentalScore)
                                            }
                                            view?.findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar3)?.apply {
                                                visibility = View.VISIBLE
                                                setProgress(socialScore)
                                            }

                                            view?.findViewById<TextView>(R.id.physicalScore)?.text = "Physical:\n$physicalScore%"
                                            view?.findViewById<TextView>(R.id.mentalScore)?.text = "Mental:\n$mentalScore%"
                                            view?.findViewById<TextView>(R.id.socialScore)?.text = "Social:\n$socialScore%"
                                        } else {
                                            // Load progress from SharedPreferences if there are not enough scores
                                            loadProgressFromPreferences()
                                            setLoading(false, binding.progressBar1)
                                            binding.speedometerAlert.visibility = View.VISIBLE
                                            binding.llSpeedometer.visibility = View.GONE
                                        }
                                        return@addOnSuccessListener // Exit after processing the scores
                                    } else {
                                        setLoading(false, binding.progressBar1)
                                        binding.speedometerAlert.visibility = View.VISIBLE
                                        binding.llSpeedometer.visibility = View.GONE
                                    }
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Error fetching data", e)
                    // Load progress from SharedPreferences in case of error
                    loadProgressFromPreferences()
                    setLoading(false, binding.progressBar1)
                    binding.speedometerAlert.visibility = View.VISIBLE
                    binding.llSpeedometer.visibility = View.GONE
                }
        } else {
            Log.e(tag, "User not logged in")
            // Load progress from SharedPreferences if the user is not logged in
            loadProgressFromPreferences()
        }
    }



    fun setLoading(loading: Boolean, progressBar: ProgressBar) {
        if (loading) {
            progressBar.visibility = View.VISIBLE // Show the ProgressBar
        } else {
            progressBar.visibility = View.GONE // Hide the ProgressBar
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}



//package com.example.aiimshealthapp
//
//import android.content.Intent
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.LinearLayout
//import android.widget.ProgressBar
//import android.widget.TextView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.tasks.Tasks
//import com.google.firebase.Firebase
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.auth
//import kotlin.random.Random
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.QuerySnapshot
//
//class Dashboard : AppCompatActivity() {
//    private val db = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//    private val currentUser = auth.currentUser
//    private lateinit var progressBar1 : ProgressBar
//    private lateinit var progressBar2 : ProgressBar
//    private lateinit var speedometer_content: LinearLayout
//    private lateinit var factView: TextView
//    private lateinit var sourceView: TextView
//    private lateinit var topicView: TextView
//    private val tag = "CHECK_RESPONSE"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_dashboard)
//        progressBar1 = findViewById<ProgressBar>(R.id.progressBar1)
//        progressBar2 = findViewById<ProgressBar>(R.id.progressBar2)
//        speedometer_content = findViewById<LinearLayout>(R.id.speedometer_content)
//        factView = findViewById<TextView>(R.id.tvFact)
//        sourceView = findViewById<TextView>(R.id.tvSource)
//        topicView = findViewById<TextView>(R.id.tvTopic)
//
//        setLoading(true, progressBar1)
//        speedometer_content.visibility = View.GONE
//
//        updateProgressBars()
//
//        setLoading(true,progressBar2)
//        factView.visibility = View.GONE
//        sourceView.visibility = View.GONE
//        topicView.visibility = View.GONE
//        getFactList { (facts, sources, topics) ->
//            setLoading(false,progressBar2)
//            factView.visibility = View.VISIBLE
//            sourceView.visibility = View.VISIBLE
//            topicView.visibility = View.VISIBLE
//            val handler = Handler(Looper.getMainLooper())
//            val runnable = object : Runnable {
//                override fun run() {
//                    if (facts.isNotEmpty() && sources.isNotEmpty() && topics.isNotEmpty()) {
//                        val index = Random.nextInt(0, facts.size)
//                        factView.text = facts[index]
//                        sourceView.text = sources[index]
//                        topicView.text = topics[index]
//                    } else {
//                        // Handle the case where the lists are empty
//                        Log.e("Dashboard", "One or more lists are empty!")
//                        factView.text = "No facts available"
//                        sourceView.text = "No source available"
//                        topicView.text = "No topic available"
//                    }
//                    handler.postDelayed(this, 3000)
//                }
//            }
//            handler.post(runnable)
//        }
//        factView.setOnClickListener{
//            val intent = Intent(this, HealthInfoActivity::class.java)
//            startActivity(intent)
//        }
////        val signout = findViewById<Button>(R.id.signout)
////        signout.setOnClickListener{
////            Firebase.auth.signOut()
////        }
//
//
//    }
//
//    private fun getFactList(onFactsLoaded: (Triple<List<String>, List<String>, List<String>>) -> Unit) {
//        val factsList = mutableListOf<String>()
//        val sourceList = mutableListOf<String>()
//        val topicList = mutableListOf<String>()
//        var metrics: Map<String, Any> = emptyMap()
//
//        val fullName = findViewById<TextView>(R.id.fullName)
//        val username = findViewById<TextView>(R.id.username)
//
//        if (currentUser != null) {
//            // Fetch both collections in parallel to speed up the process
//            val metricsTask = db.collection("metrics")
//                .whereEqualTo("user.email", currentUser.email)
//                .get()
//
//            val factsTask = db.collection("fact_collection").get()
//
//            // When both tasks are done
//            Tasks.whenAllSuccess<QuerySnapshot>(metricsTask, factsTask)
//                .addOnSuccessListener { results ->
//                    // Process metrics first
//                    val metricsDocuments = results[0] as QuerySnapshot
//                    if (!metricsDocuments.isEmpty) {
//                        val document = metricsDocuments.documents[0]
//                        val data = document.data ?: emptyMap()
//                        metrics = (data["metrics"] as? Map<String, Any>)?.toMap() ?: emptyMap()
//                        fullName.text = "${metrics["firstName"]} ${metrics["lastName"]}"
//                        username.text = currentUser.email.toString().split("@")[0]
//
//                    }
//
//
//                    // Now process facts
//                    val factsDocuments = results[1] as QuerySnapshot
//                    for (document in factsDocuments) {
//                        val data = document.data
//                        val facts = data["facts"] as? List<Map<String, Any>>
//                        if (facts != null) {
//                            for (fact in facts) {
//                                // Skip facts based on metrics filtering conditions
//                                val skipFact = when {
//                                    metrics["smoking"] == false && fact["topic"].toString() == "Smoking" -> true
//                                    metrics["alcohol"] == false && fact["topic"].toString() == "Alcohol" -> true
//                                    metrics["diabetes"] == "0" && fact["topic"].toString() == "Diabetes" -> true
//                                    metrics["hypertension"] == "0" && fact["topic"].toString() == "Hypertension" -> true
//                                    else -> false
//                                }
//
//                                if (!skipFact) {
//                                    factsList.add(fact["fact"].toString())
//                                    sourceList.add(fact["source"].toString())
//                                    topicList.add(fact["topic"].toString())
//                                }
//                            }
//                        }
//                    }
//
//                    // Trigger the callback with the populated lists
//                    onFactsLoaded(Triple(factsList, sourceList, topicList))
//                }
//                .addOnFailureListener { e ->
//                    Log.e(tag, "Error fetching data: ", e)
//                    onFactsLoaded(Triple(emptyList(), emptyList(), emptyList())) // Send empty lists on failure
//                }
//        } else {
//            onFactsLoaded(Triple(emptyList(), emptyList(), emptyList())) // Send empty lists if user not authenticated
//        }
//    }
//
//
//
//
//
//    private fun updateProgressBars() {
//        val scores = mutableListOf<Int>()
//
//        if (currentUser != null) {
//            db.collection("questions2")
//                .whereEqualTo("user.email", currentUser.email)
//                .get()
//                .addOnSuccessListener { documents ->
//                    for (document in documents) {
//                        val data = document.data
//                        val quizes = data["quizes"] as? List<Map<String, Any>>
//                        if (quizes != null) {
//                            for (quiz in quizes) {
//                                val score = (quiz["score"] as? Number)?.toInt() ?: 0
//                                scores.add(score)
//                            }
//
//                            // Ensure there are at least 3 quiz scores
//                            if (scores.size >= 3) {
//                                // Calculate the scores with boundaries
//                                val physicalScore = ((scores[0].toDouble() / 600) * 100).coerceIn(0.0, 100.0).toInt()
//                                val mentalScore = (((scores[1] - 7).toDouble() / (35 - 7)) * 100).coerceIn(0.0, 100.0).toInt()
//                                val socialScore = (100 - ((scores[2].toDouble() / 6) * 100)).coerceIn(0.0, 100.0).toInt()
//
//                                // Update UI elements
//                                setLoading(false, progressBar1)
//                                speedometer_content.visibility = View.VISIBLE
//                                findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar1).visibility = View.VISIBLE
//                                findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar2).visibility = View.VISIBLE
//                                findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar3).visibility = View.VISIBLE
//                                findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar1).setProgress(physicalScore)
//                                findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar2).setProgress(mentalScore)
//                                findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar3).setProgress(socialScore)
//                                findViewById<FullCircleProgressBar>(R.id.fullCircleProgressBar4).setProgress(40)
//
//                                findViewById<TextView>(R.id.physcialScore).text = "Physical:\n$physicalScore%"
//                                findViewById<TextView>(R.id.mentalScore).text = "Mental:\n$mentalScore%"
//                                findViewById<TextView>(R.id.socialScore).text = "Social:\n$socialScore%"
//                            } else {
//                                Log.e("updateProgressBars", "Not enough quiz scores to calculate progress")
//                            }
//                        }
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Log.e("updateProgressBars", "Error fetching data", e)
//                }
//        } else {
//            Log.e("updateProgressBars", "User not logged in")
//        }
//    }
//
//    fun setLoading(loading: Boolean, progressBar: ProgressBar) {
//        if (loading) {
//            progressBar.visibility = View.VISIBLE // Show the ProgressBar
//        } else {
//            progressBar.visibility = View.GONE // Hide the ProgressBar
//        }
//    }
//}
package com.example.aiimshealthapp
import android.os.Bundle


import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.databinding.ActivityDashboardBinding

class Dashboard : AppCompatActivity() {
    lateinit var binding : ActivityDashboardBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        setContentView(binding.root)
        replaceFragment(DashboardFragment())
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Handle home navigation
                    replaceFragment(DashboardFragment())
                }
                R.id.menu_explore -> {
                    // Handle explore navigation
                    replaceFragment(StepTrackerFragment())
                }
                R.id.menu_notification -> {
                    // Handle notification navigation
                    replaceFragment(PostureExerciseFragment())
                }
                R.id.menu_settings -> {
                    // Handle settings navigation
                    replaceFragment(DashboardFragment())
                }
            }
            true
        }


    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

}


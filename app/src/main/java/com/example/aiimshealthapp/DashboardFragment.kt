package com.example.aiimshealthapp

import android.content.Intent
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
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private lateinit var progressBar1 : ProgressBar
    private lateinit var progressBar2 : ProgressBar
    private lateinit var speedometer_content: LinearLayout
    private lateinit var factView: TextView
    private lateinit var sourceView: TextView
    private lateinit var topicView: TextView
    private val tag = "CHECK_RESPONSE"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        progressBar1 = view.findViewById(R.id.progressBar1)
        progressBar2 = view.findViewById(R.id.progressBar2)
        speedometer_content = view.findViewById(R.id.speedometer_content)
        factView = view.findViewById(R.id.tvFact)
        sourceView = view.findViewById(R.id.tvSource)
        topicView = view.findViewById(R.id.tvTopic)

        // Call necessary methods in onViewCreated
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLoading(true, progressBar1)
        speedometer_content.visibility = View.GONE
        updateProgressBars()

        setLoading(true, progressBar2)
        factView.visibility = View.GONE
        sourceView.visibility = View.GONE
        topicView.visibility = View.GONE
        getFactList { (facts, sources, topics) ->
            setLoading(false, progressBar2)
            factView.visibility = View.VISIBLE
            sourceView.visibility = View.VISIBLE
            topicView.visibility = View.VISIBLE
            val handler = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    if (facts.isNotEmpty() && sources.isNotEmpty() && topics.isNotEmpty()) {
                        val index = Random.nextInt(0, facts.size)
                        factView.text = facts[index]
                        sourceView.text = sources[index]
                        topicView.text = topics[index]
                    } else {
                        // Handle the case where the lists are empty
                        Log.e("DashboardFragment", "One or more lists are empty!")
                        factView.text = "No facts available"
                        sourceView.text = "No source available"
                        topicView.text = "No topic available"
                    }
                    handler.postDelayed(this, 3000)
                }
            }
            handler.post(runnable)
        }

        val signout = view.findViewById<Button>(R.id.signout)
        signout.setOnClickListener{
            Firebase.auth.signOut()
            val intent = Intent(requireContext(), LoginWithUsername::class.java)
            startActivity(intent)
        }

        factView.setOnClickListener {
            val intent = Intent(requireContext(), HealthInfoActivity::class.java)
            startActivity(intent)
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
        val scores = mutableListOf<Int>()

        if (currentUser != null) {
            db.collection("questions2")
                .whereEqualTo("user.email", currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val data = document.data
                        val quizes = data["quizes"] as? List<Map<String, Any>>
                        if (quizes != null) {
                            for (quiz in quizes) {
                                val score = (quiz["score"] as? Number)?.toInt() ?: 0
                                scores.add(score)
                            }

                            if (scores.size >= 3) {
                                val physicalScore = ((scores[0].toDouble() / 600) * 100).coerceIn(0.0, 100.0).toInt()
                                val mentalScore = (((scores[1] - 7).toDouble() / (35 - 7)) * 100).coerceIn(0.0, 100.0).toInt()
                                val socialScore = (100 - ((scores[2].toDouble() / 6) * 100)).coerceIn(0.0, 100.0).toInt()

                                setLoading(false, progressBar1)
                                speedometer_content.visibility = View.VISIBLE
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

                                view?.findViewById<TextView>(R.id.physcialScore)?.text = "Physical:\n$physicalScore%"
                                view?.findViewById<TextView>(R.id.mentalScore)?.text = "Mental:\n$mentalScore%"
                                view?.findViewById<TextView>(R.id.socialScore)?.text = "Social:\n$socialScore%"
                            } else {
                                Log.e("updateProgressBars", "Not enough quiz scores to calculate progress")
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("updateProgressBars", "Error fetching data", e)
                }
        } else {
            Log.e("updateProgressBars", "User not logged in")
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
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HealthInfoPageActivity : AppCompatActivity() {
    private var viewIds = mutableListOf<Int>()
    private lateinit var mainLayout: RelativeLayout
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val collection = "fact_collection"
    private var maxCardViewId = 0
    private val tag = "CHECK_RESPONSE"
    private lateinit var progressBar : ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_health_info_page)
        mainLayout = findViewById(R.id.main_layout)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        updatePage()
        setLoading(true, progressBar)
    }


    private fun updatePage(){
        val given_topic = intent.getStringExtra("topic")
        if(currentUser!=null){
            db.collection(collection)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        for (document in querySnapshot) {
                            val data = document.data
                            val facts = data["facts"] as? List<Map<String, Any>>
                            if (facts != null) {
                                for(fact in facts){
                                    val topic = fact["topic"] as? String
                                    if(topic == given_topic){
                                        val title = fact["fact"] as? String
                                        val source = fact["source"] as? String
                                        generateUniqueId { count ->
                                            val uniqueId = count + 1
                                            if (title != null) {
                                                setLoading(false, progressBar)
                                                generateTimeLayout(uniqueId, title, source.toString())
                                            }
                                        }


                                    }
                                }
                            }
                        }
                    } else {
                        Log.i(tag, "No documents found in the collection.")
                    }

                }
        }
    }

    private fun generateTimeLayout(cardViewId: Int, title: String, source: String) {

        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.card_fact_view, mainLayout, false) as CardView

        cardView.id = cardViewId
        viewIds.add(cardViewId)

        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        if (viewIds.size > 1) {
            params.topMargin = 16
            params.addRule(RelativeLayout.BELOW, viewIds[viewIds.size - 2])
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        }

        cardView.layoutParams = params
        val factView = cardView.findViewById<TextView>(R.id.factView)
        factView.text = title
        val sourceView = cardView.findViewById<TextView>(R.id.sourceView)
        sourceView.text = source

        mainLayout.addView(cardView)


    }

    private fun generateUniqueId(callback: (Int) -> Unit) {
        maxCardViewId += 1
        callback(maxCardViewId)
    }

    fun setLoading(loading: Boolean, progressBar: ProgressBar) {
        if (loading) {
            progressBar.visibility = View.VISIBLE // Show the ProgressBar
        } else {
            progressBar.visibility = View.GONE // Hide the ProgressBar
        }
    }
}
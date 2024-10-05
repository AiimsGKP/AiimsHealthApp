package com.example.aiimshealthapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PostureExerciseFragment2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_posture_exercise2, container, false)

        // Setup RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        // Sample data to populate the cards
        val cardLists = listOf(
            mapOf("title" to "Bruegger's Relief Position",
                "description" to "This stretch can help reverse a rounded forward position.",
                "duration" to "60",
                "color" to "#ed3241"),
            mapOf("title" to "Chin Tuck",
                "description" to "Classic exercise to get out of a forward head position",
                "duration" to "50",
                "color" to "#3ac0a0")
        )

        // Setup the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up the adapter with click handling
        recyclerView.adapter = PostureAdapter(cardLists) { selectedCard ->
            // Navigate to the detail fragment and pass the selected card data
            navigateToDetailFragment(selectedCard)
        }

        return view
    }

    private fun navigateToDetailFragment(cardData: Map<String, String>) {
        val detailFragment = PostureDetailFragment()

        // Pass the card data using arguments
        val bundle = Bundle()
        bundle.putString("title", cardData["title"])
        bundle.putString("description", cardData["description"])
        bundle.putString("duration", cardData["duration"])
        bundle.putString("color", cardData["color"])
        detailFragment.arguments = bundle

        // Navigate to the detail fragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, detailFragment) // Update with your actual container ID
            .addToBackStack(null)
            .commit()
    }
}


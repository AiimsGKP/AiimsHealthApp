package com.example.aiimshealthapp.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.aiimshealthapp.R

class PostureDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_posture_detail, container, false)

        // Retrieve the data from arguments
        val title = arguments?.getString("title")
        val description = arguments?.getString("description")
        val duration = arguments?.getString("duration")
        val color = arguments?.getString("color")
        val steps = arguments?.getString("steps")

        // Bind the data to the views
        val titleTextView: TextView = view.findViewById(R.id.detailTitle)
        val descriptionTextView: TextView = view.findViewById(R.id.detailDescription)
        val durationTextView: TextView = view.findViewById(R.id.detailDuration)
        val stepsTextView: TextView = view.findViewById(R.id.steps)

        titleTextView.text = title ?: "No Title"
        descriptionTextView.text = description ?: "No Description"
        durationTextView.text = "${duration ?: "0"} secs"
        stepsTextView.text = steps.toString()

        Log.i(tag, steps.toString())


        // Optionally, set the background color if needed
//        view.setBackgroundColor(Color.parseColor(color ?: "#FFFFFF")) // Default to white

        return view
    }
}
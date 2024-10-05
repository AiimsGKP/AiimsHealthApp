package com.example.aiimshealthapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

class PostureExerciseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_posture_exercise, container, false)

        val postureExercise = view.findViewById<ConstraintLayout>(R.id.posture_exercise)
        val guidance = view.findViewById<ConstraintLayout>(R.id.guidance)

        postureExercise.setOnClickListener(){
            val secondFragment = PostureExerciseFragment2()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                .addToBackStack(null) // Optional: Adds the transaction to the back stack so the user can navigate back
                .commit()
        }

        guidance.setOnClickListener(){
            val secondFragment = GuidanceFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, secondFragment) // R.id.fragment_container is the ID of the container in the activity layout
                .addToBackStack(null) // Optional: Adds the transaction to the back stack so the user can navigate back
                .commit()
        }



        return view
    }

}
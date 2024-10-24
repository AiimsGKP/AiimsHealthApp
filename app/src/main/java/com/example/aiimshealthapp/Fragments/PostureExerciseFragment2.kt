package com.example.aiimshealthapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aiimshealthapp.PostureAdapter
import com.example.aiimshealthapp.R

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
            mapOf("title" to "Shoulder Blade Squeeze",
                "description" to "The shoulder blade squeeze is a simple exercise that can help improve posture by strengthening the muscles in the upper back and shoulders.",
                "duration" to "60",
                "color" to "#ed3241",
                "steps" to "1.\tBegin by sitting or standing with your back straight and your arms by your sides.\n" +
                        "2.\tGently draw your shoulder blades back and down, squeezing them together.\n" +
                        "3.\tHold this position for 5-10 seconds while keeping your back straight and your shoulders relaxed.\n" +
                        "4.\tRelease the squeeze and allow your shoulders to relax back to their starting position.\n" +
                        "5.\tRepeat this exercise for 2-3 sets of 10-15 repetitions, taking a short break between each set.\n"),
            mapOf("title" to "Chin Tuck",
                "description" to "The chin tuck is a simple exercise that can help correct posture and alleviate neck pain.",
                "duration" to "60",
                "color" to "#3ac0a0",
                "steps" to "1.\tBegin by sitting or standing with your back straight and your shoulders relaxed.\n" +
                        "2.\tLook straight ahead and imagine a string attached to the top of your head, gently pulling your head upwards.\n" +
                        "3.\tSlowly tuck your chin inwards towards your neck, while keeping your eyes looking straight ahead.\n" +
                        "4.\tHold this position for 5-10 seconds, while feeling a gentle stretch at the base of your neck.\n" +
                        "5.\tRelease the tuck and return your head to its starting position.\n" +
                        "6.\tRepeat this exercise for 2-3 sets of 10-15 repetitions, taking a short break between each set.\n"),
            mapOf("title" to "Chest Stretch",
                "description" to "The chest stretch is a simple exercise that can help improve posture by stretching the muscles in the chest and shoulders. It's a great exercise for anyone who spends a lot of time sitting or hunched over a desk.",
                "duration" to "60",
                "color" to "#3ac0a0",
                "steps" to "1.\tBegin by standing up straight with your feet shoulder-width apart.\n" +
                        "2.\tInterlace your fingers behind your back and straighten your arms.\n" +
                        "3.\tSqueeze your shoulder blades together and lift your arms up and away from your body.\n" +
                        "4.\tKeep your chest open and your shoulders relaxed.\n" +
                        "5.\tHold the stretch for 15-30 seconds while taking deep breaths.\n" +
                        "6.\tRelease the stretch and return your arms to your sides.\n" +
                        "7.\tRepeat this exercise for 2-3 sets of 10-15 repetitions, taking a short break between each set.\n"),

            mapOf("title" to "Cat-Cow Stretch",
                "description" to "The cat-cow stretch is a yoga-inspired exercise that can help improve posture by increasing flexibility and mobility in the spine. It's a great exercise for anyone who wants to improve their spinal health and reduce tension in the back.",
                "duration" to "60",
                "color" to "#3ac0a0",
                "steps" to "1.\tBegin on your hands and knees with your wrists directly under your shoulders and your knees directly under your hips.\n" +
                        "2.\tAs you inhale, lift your tailbone up towards the ceiling, arch your back, and let your head hang down towards the floor - this is the cow pose.\n" +
                        "3.\tAs you exhale, tuck your chin to your chest, round your spine towards the ceiling, and bring your belly button towards your spine - this is the cat pose.\n" +
                        "4.\tContinue moving between the cow pose and the cat pose, inhaling as you move into the cow pose and exhaling as you move into the cat pose.\n" +
                        "5.\tRepeat this exercise for 2-3 sets of 10-15 repetitions, taking a short break between each set.\n"),

            mapOf("title" to "Plank",
                "description" to "The plank is a popular exercise that can help improve posture by strengthening the core muscles, which support the spine and keep the body in proper alignment. It's a great exercise for anyone who wants to improve their overall posture and core strength.",
                "duration" to "60",
                "color" to "#3ac0a0",
                "steps" to "1.\tBegin in a push-up position with your hands shoulder-width apart, and your toes tucked under.\n" +
                        "2.\tEngage your core and glutes to keep your body in a straight line from your head to your heels.\n" +
                        "3.\tKeep your neck and spine in a neutral position by looking down at the floor.\n" +
                        "4.\tHold the position for 30 seconds to 1 minute, or as long as you can maintain good form.\n" +
                        "5.\tAs you progress, try to hold the position for longer periods of time or add variations, such as lifting one leg or arm off the ground.\n" +
                        "6.\tRepeat this exercise for 2-3 sets of 10-15 repetitions, taking a short break between each set.\n"),
            mapOf("title" to "Glute Bridge",
                "description" to "The glute bridge is a simple exercise that can help improve posture by strengthening the muscles in the lower back, glutes, and hips. It's a great exercise for anyone looking to improve their posture and core stability.",
                "duration" to "60",
                "color" to "#3ac0a0",
                "steps" to "1.\tLie flat on your back with your arms at your sides, knees bent, and feet flat on the ground.\n" +
                        "2.\tEngage your core and glutes, and push your hips up towards the ceiling, keeping your shoulders and feet flat on the ground.\n" +
                        "3.\tHold the position for 1-2 seconds, squeezing your glutes at the top of the movement.\n" +
                        "4.\tSlowly lower your hips back down to the starting position.\n" +
                        "5.\tRepeat for 2-3 sets of 10-15 repetitions.\n"),
            mapOf("title" to "Wall Angels",
                "description" to "Wall angels are a simple and effective exercise for improving posture and reducing upper back pain. It's a great exercise for anyone who spends a lot of time sitting or hunching over a desk.",
                "duration" to "60",
                "color" to "#3ac0a0",
                "steps" to "1.\tStand with your back against a flat wall, with your feet about 6 inches away from the wall and your knees slightly bent.\n" +
                        "2.\tBring your arms up to shoulder level, with your elbows bent at a 90-degree angle and your hands resting against the wall.\n" +
                        "3.\tSlowly move your arms up and down in a \"snow angel\" motion, keeping your hands and elbows in contact with the wall at all times.\n" +
                        "4.\tMake sure to keep your shoulders relaxed and your back flat against the wall throughout the exercise.\n" +
                        "5.\tRepeat for 2-3 sets of 10-15 repetitions.\n"),
            mapOf("title" to "Seated Twist",
                "description" to "The seated twist is a simple yet effective exercise for improving spinal mobility and reducing tension in the back and neck. It's a great exercise for anyone who spends a lot of time sitting or has tightness in the back and hips.",
                "duration" to "60",
                "color" to "#3ac0a0",
                "steps" to "1.\tSit on the floor with your legs crossed in front of you.\n" +
                        "2.\tPlace your left hand on your right knee and your right hand on the floor behind your right hip.\n" +
                        "3.\tInhale and sit up tall, lengthening your spine.\n" +
                        "4.\tAs you exhale, twist your torso to the right, using your left hand to gently push against your right knee to deepen the stretch.\n" +
                        "5.\tHold for a few breaths, then inhale and come back to the centre.\n" +
                        "6.\tRepeat on the other side, placing your right hand on your left knee and your left hand on the floor behind your left hip.\n" +
                        "7.\tContinue alternating sides for 2-3 sets of 10-15 repetitions.\n"),
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
        bundle.putString("steps", cardData["steps"])
        detailFragment.arguments = bundle

        // Navigate to the detail fragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, detailFragment) // Update with your actual container ID
            .addToBackStack(null)
            .commit()
    }
}
package com.example.aiimshealthapp.Fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.databinding.FragmentBreathingPageBinding
import com.example.aiimshealthapp.models.FoodData

class BreathingPageFragment : Fragment() {
    private var _binding: FragmentBreathingPageBinding? = null
    private val binding get() = _binding!!

    private var inhale = 4L
    private var hold = 4L
    private var exhale = 8L
    private var holdAfterExhale = 4L
    private var cycles = 2
    private var title = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentBreathingPageBinding.inflate(inflater, container, false)
        arguments?.let { bundle ->
            inhale = bundle.getString("inhale")?.toLong()!!
            hold = bundle.getString("hold")?.toLong()!!
            exhale = bundle.getString("exhale")?.toLong()!!
            holdAfterExhale = bundle.getString("hold2")?.toLong()!!
            cycles = bundle.getString("cycles")?.toInt()!!
            title = bundle.getString("title").toString()
        }
        runCycles(cycles)
        binding.repeatBtn.setOnClickListener {
            runCycles(cycles)
            binding.progressBar.setProgress(0, 2000)
            binding.repeatBtn.visibility = View.GONE
        }
        binding.backBtn.setOnClickListener{
            val secondFragment = BreathingExercisesFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, secondFragment)
                .commit()

        }
        binding.title.text = title
        return binding.root
    }
    private fun startCountdownAnimation(countdownText: TextView, startNumber: Int) {
        val countdown = (startNumber downTo 1).map { it.toString() }.toTypedArray()

        object : CountDownTimer(startNumber * 1000L, 1000) {
            var index = 0

            override fun onTick(millisUntilFinished: Long) {
                if (index < countdown.size) {  // Ensure index is within bounds
                    countdownText.text = countdown[index]
                    index++
                }
            }

            override fun onFinish() {
                countdownText.text = ""
            }
        }.start()
    }


    private fun animateTextView(textView: TextView) {
        // Animate scale (zoom in and out)
        ObjectAnimator.ofFloat(textView, View.SCALE_X, 1f, 1.5f, 1f).apply {
            duration = 500
            start()
        }

        ObjectAnimator.ofFloat(textView, View.SCALE_Y, 1f, 1.5f, 1f).apply {
            duration = 500
            start()
        }

        // Optional: Animate fade out and fade in (alpha)
        ObjectAnimator.ofFloat(textView, View.ALPHA, 1f, 0.5f, 1f).apply {
            duration = 500
            start()
        }
    }
    private fun runCycles(n: Int) {
        startCountdownAnimation(binding.countDown1, 3) // Start the countdown animation

        // Start the countdown timer
        object : CountDownTimer(n * 1500L, 1500) { // Each cycle lasts 4000 milliseconds
            var cycle = 1 // Track the current cycle

            override fun onTick(millisUntilFinished: Long) {
                // No action needed during countdown ticks
            }

            override fun onFinish() {
                // Trigger the circle animation for each cycle
                binding.textView.text = "Relax and get comfortable"
                binding.circle.animate().apply {
                    duration = 8000  // Inhale duration
                    interpolator = AccelerateDecelerateInterpolator()
                    scaleX(0.5f)  // Decrease to half its size
                    scaleY(0.5f)  // Decrease to half its size
                }.withEndAction {
                    // Hold at half size before exhaling
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.circle.animate().apply {
                            duration = 8000  // Exhale duration
                            interpolator = AccelerateDecelerateInterpolator()
                            scaleX(1f)  // Return to normal size
                            scaleY(1f)  // Return to normal size
                        }.withEndAction {

                            if (cycle < n) {
                                // Trigger the next cycle after a short delay
                                Handler(Looper.getMainLooper()).postDelayed({
                                    animateCircle(cycle + 1, n) // Call animateCircle for the next cycle
                                }, 0) // Delay for the next cycle
                            } else {
                                updateText("")
                                binding.repeatBtn.visibility = View.VISIBLE
                            }
                        }.start()
                    }, 1000) // Delay before exhaling
                }.start()
                Handler(Looper.getMainLooper()).postDelayed({
                    animateCircle(cycle, n)
                }, 10000)
            }
        }.start()
    }

    private fun animateCircle(cycle: Int, n: Int) {
        // Start animation to reduce size (inhale)
        updateText("Inhale")
        startCountdownAnimation(binding.countDown2, inhale.toInt())
        binding.circle.animate().apply {
            duration = inhale * 2000  // Inhale duration (convert to milliseconds)
            interpolator = AccelerateDecelerateInterpolator()
            scaleX(0.5f)  // Decrease to half its size
            scaleY(0.5f)  // Decrease to half its size
        }.withEndAction {
            // This is where the hold phase starts after inhaling
            updateText("Hold")
            if (hold > 0) binding.countDown2.text = "||"

            // Run code during the hold phase
            Handler(Looper.getMainLooper()).postDelayed({
                // After hold, proceed to exhale
                updateText("Exhale")
                startCountdownAnimation(binding.countDown2, exhale.toInt())
                binding.circle.animate().apply {
                    duration = exhale * 2000  // Exhale duration
                    interpolator = AccelerateDecelerateInterpolator()
                    scaleX(1f)  // Return to normal size
                    scaleY(1f)  // Return to normal size
                }.withEndAction {
                    // Clear text after exhaling
                    updateText("")

                    // Add additional hold after exhale
                    Handler(Looper.getMainLooper()).postDelayed({
                        updateText("Hold")
                        if (holdAfterExhale > 0) binding.countDown2.text = "||"

                        // Hold duration after exhale
                        Handler(Looper.getMainLooper()).postDelayed({
                            // Clear text after hold
                            updateText("")
                            binding.countDown2.text = ""

                            // Update progress bar after the circle animation is complete
                            binding.progressBar.setProgress(
                                ((100 / n.toDouble()) * cycle).toInt(), 2000
                            )

                            // Check if there are more cycles
                            if (cycle < n) {
                                // Trigger the next cycle after a short delay
                                Handler(Looper.getMainLooper()).postDelayed({
                                    animateCircle(cycle + 1, n)  // Call animateCircle for the next cycle
                                }, 0)  // Delay for the next cycle
                            } else {
                                updateText("")
                                // Make sure both visibility and enable are set correctly
                                binding.repeatBtn.apply {
                                    visibility = View.VISIBLE
                                    isEnabled = true
                                }
                            }
                        }, holdAfterExhale * 1000) // Delay for the new hold phase
                    }, 0) // Optional delay after exhale
                }.start()
            }, hold * 1000)  // Delay for the hold phase (convert to milliseconds)
        }.start()
    }





    private fun updateText(text:String) {
        // Update the text views as needed. For example:
        binding.textView.text = text
    }


}
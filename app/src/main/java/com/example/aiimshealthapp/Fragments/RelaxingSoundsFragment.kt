package com.example.aiimshealthapp.Fragments

import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.databinding.FragmentRelaxingSoundsBinding

class RelaxingSoundsFragment : Fragment() {
    private var _binding: FragmentRelaxingSoundsBinding? = null
    private val binding get() = _binding!!

    // Separate MediaPlayer instances for each sound
    private var thunderMedia: MediaPlayer? = null
    private var forestMedia: MediaPlayer? = null
    private var rainMedia: MediaPlayer? = null
    private var fireMedia: MediaPlayer? = null
    private var windMedia: MediaPlayer? = null
    private var wavesMedia: MediaPlayer? = null

    private var isThunderSelected: Boolean = true
    private var isForestSelected: Boolean = true
    private var isRainSelected: Boolean = true
    private var isFireSelected: Boolean = true
    private var isWindSelected: Boolean = true
    private var isWavesSelected: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRelaxingSoundsBinding.inflate(inflater, container, false)

        // button listeners
        binding.backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack() // Pops the top fragment from the back stack
        }

        binding.btnThunder.setOnClickListener {
            toggleSound(R.raw.thunder, binding.btnThunder, binding.vThunder, binding.tvThunder) { isThunderSelected }
            binding.llThunder.visibility = if(isThunderSelected) View.VISIBLE else View.GONE
            isThunderSelected = !isThunderSelected
        }

        binding.btnForest.setOnClickListener {
            toggleSound(R.raw.forest, binding.btnForest, binding.vForest, binding.tvForest) { isForestSelected }
            binding.llForest.visibility = if(isForestSelected) View.VISIBLE else View.GONE
            isForestSelected = !isForestSelected
        }

        binding.btnRain.setOnClickListener {
            toggleSound(R.raw.rain, binding.btnRain, binding.vRain, binding.tvRain) { isRainSelected }
            binding.llRain.visibility = if(isRainSelected) View.VISIBLE else View.GONE
            isRainSelected = !isRainSelected
        }

        binding.btnFire.setOnClickListener {
            toggleSound(R.raw.fire, binding.btnFire, binding.vFire, binding.tvFire) { isFireSelected }
            binding.llFire.visibility = if(isFireSelected) View.VISIBLE else View.GONE
            isFireSelected = !isFireSelected
        }

        binding.btnWind.setOnClickListener {
            toggleSound(R.raw.wind, binding.btnWind, binding.vWind, binding.tvWind) { isWindSelected }
            binding.llWind.visibility = if(isWindSelected) View.VISIBLE else View.GONE
            isWindSelected = !isWindSelected
        }

        binding.btnWaves.setOnClickListener {
            toggleSound(R.raw.waves, binding.btnWaves, binding.vWaves, binding.tvWaves) { isWavesSelected }
            binding.llWaves.visibility = if(isWavesSelected) View.VISIBLE else View.GONE
            isWavesSelected = !isWavesSelected
        }


        // Seekbar listeners
        binding.seekBarThunder.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f // Convert to float from 0.0 to 1.0
                thunderMedia?.setVolume(volume, volume) // Set the same volume for both channels
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.seekBarForest.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f // Convert to float from 0.0 to 1.0
                forestMedia?.setVolume(volume, volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.seekBarRain.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f // Convert to float from 0.0 to 1.0
                rainMedia?.setVolume(volume, volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.seekBarFire.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f // Convert to float from 0.0 to 1.0
                fireMedia?.setVolume(volume, volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.seekBarWind.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f // Convert to float from 0.0 to 1.0
                windMedia?.setVolume(volume, volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.seekBarWaves.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f // Convert to float from 0.0 to 1.0
                wavesMedia?.setVolume(volume, volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        return binding.root
    }

    private fun toggleSound(soundResId: Int, button: View, view: View, textView: TextView, isSelected: () -> Boolean) {
        if (isSelected()) {
            playSound(soundResId, button, view, textView)
        } else {
            pauseSound(soundResId)
            resetButtonState(button, view, textView)
        }
    }

    private fun playSound(soundResId: Int, button: View, view: View, textView: TextView) {
        when (soundResId) {
            R.raw.thunder -> {
                if (thunderMedia == null) {
                    thunderMedia = MediaPlayer.create(requireContext(), soundResId)
                    thunderMedia?.isLooping = true // Enable looping
                }
                if (!thunderMedia!!.isPlaying) {
                    thunderMedia?.start()
                }
            }
            R.raw.forest -> {
                if (forestMedia == null) {
                    forestMedia = MediaPlayer.create(requireContext(), soundResId)
                    forestMedia?.isLooping = true // Enable looping
                }
                if (!forestMedia!!.isPlaying) {
                    forestMedia?.start()
                }
            }
            R.raw.rain -> {
                if (rainMedia == null) {
                    rainMedia = MediaPlayer.create(requireContext(), soundResId)
                    rainMedia?.isLooping = true // Enable looping
                }
                if (!rainMedia!!.isPlaying) {
                    rainMedia?.start()
                }
            }
            R.raw.fire -> {
                if (fireMedia == null) {
                    fireMedia = MediaPlayer.create(requireContext(), soundResId)
                    fireMedia?.isLooping = true // Enable looping
                }
                if (!fireMedia!!.isPlaying) {
                    fireMedia?.start()
                }
            }
            R.raw.wind -> {
                if (windMedia == null) {
                    windMedia = MediaPlayer.create(requireContext(), soundResId)
                    windMedia?.isLooping = true // Enable looping
                }
                if (!windMedia!!.isPlaying) {
                    windMedia?.start()
                }
            }
            R.raw.waves -> {
                if (wavesMedia == null) {
                    wavesMedia = MediaPlayer.create(requireContext(), soundResId)
                    wavesMedia?.isLooping = true // Enable looping
                }
                if (!wavesMedia!!.isPlaying) {
                    wavesMedia?.start()
                }
            }
        }

        // Update UI elements
        button.setBackgroundResource(R.drawable.rounded_corner_2)
        view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.neutral_light_5))
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral_light_5))
    }

    private fun pauseSound(soundResId: Int) {
        when (soundResId) {
            R.raw.thunder -> {
                thunderMedia?.pause()
            }
            R.raw.forest -> {
                forestMedia?.pause()
            }
            R.raw.rain -> {
                rainMedia?.pause()
            }
            R.raw.fire -> {
                fireMedia?.pause()
            }
            R.raw.wind -> {
                windMedia?.pause()
            }
            R.raw.waves -> {
                wavesMedia?.pause()
            }
        }
    }

    private fun resetButtonState(button: View, view: View, textView: TextView) {
        button.setBackgroundResource(R.drawable.rounded_corner)
        view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.highlight_2))
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral_dark_1))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Release MediaPlayer resources
        thunderMedia?.release()
        forestMedia?.release()
        rainMedia?.release()
        fireMedia?.release()
        windMedia?.release()
        wavesMedia?.release()
        thunderMedia = null
        forestMedia = null
        rainMedia = null
        fireMedia = null
        windMedia = null
        wavesMedia = null
        _binding = null
    }
}

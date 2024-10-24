package com.example.aiimshealthapp.Fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aiimshealthapp.R
import com.example.aiimshealthapp.databinding.FragmentFoodScannerBinding
import com.example.aiimshealthapp.models.FoodData
import com.example.aiimshealthapp.models.RetrofitInstance
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FoodScannerFragment : Fragment() {
    private var _binding: FragmentFoodScannerBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var camera: androidx.camera.core.Camera
    private val tag = "CHECK_RESPONSE"
    private val GALLERY_REQUEST_CODE = 2
    private var barcodeDetected = false
    private var isFlashOn = false

    private val scanner: BarcodeScanner by lazy {
        BarcodeScanning.getClient()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Check for camera permission
//        fetchProductInfo("8901888006363")


        checkCameraPermission()

        binding.buttonSelectImage.setOnClickListener {
            openGallery()
        }

        binding.buttonFlash.setOnClickListener {
            toggleFlash()
        }

    }

    override fun onResume() {
        super.onResume()
        barcodeDetected = false
        startCamera()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
        } else {
            // Permission already granted
            startCamera()
        }
    }

    private fun toggleFlash() {
        if (camera.cameraInfo.hasFlashUnit()) {
            isFlashOn = !isFlashOn
            camera.cameraControl.enableTorch(isFlashOn)

            if (isFlashOn) {
                binding.buttonFlash.setBackgroundResource(R.drawable.flash_on)
            } else {
                binding.buttonFlash.setBackgroundResource(R.drawable.flash_off)
            }
        } else {
            Toast.makeText(requireContext(), "No flash available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        handleBarcodeResult(barcode)
                    }
                }
                .addOnFailureListener {
                    // Handle error
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close() // Close the proxy if no media image is available
        }
    }

    private fun handleBarcodeResult(barcode: com.google.mlkit.vision.barcode.common.Barcode) {
        // Check if a barcode has already been detected
        if (!barcodeDetected) {

            barcodeDetected = true // Set the flag to stop further processing

            val rawValue = barcode.rawValue
            // For demonstration purposes
            // Toast.makeText(context, "Scanned: $rawValue", Toast.LENGTH_SHORT).show()
            Log.i(tag, rawValue.toString())
            fetchProductInfo(rawValue.toString())

            // Stop further analysis by unbinding the camera
            cameraProvider.unbindAll() // Stop the camera preview and analysis
        }
    }


    private fun fetchProductInfo(barcode: String) {
        RetrofitInstance.api.getProductInfo(barcode).enqueue(object : Callback<FoodData> {
            override fun onResponse(call: Call<FoodData>, response: Response<FoodData>) {
                if (response.isSuccessful) {
                    val productInfo = response.body()
                    val bundle = Bundle().apply {
                        putParcelable("food_data_key", productInfo)
                    }

//                    Log.i()
                    val destinationFragment = FoodDetailsFragment().apply {
                        arguments = bundle
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, destinationFragment)
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(context, "Product Not Found!!", Toast.LENGTH_SHORT).show()
                    Log.e(tag, "Response Code: ${response.code()}")
                    startCamera()
                }
            }

            override fun onFailure(call: Call<FoodData>, t: Throwable) {
                Log.e(tag, t.message.toString())
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted, start the camera
                    startCamera()
                } else {
                    // Permission denied, handle accordingly
                    Toast.makeText(context, "Camera permission is required.", Toast.LENGTH_LONG).show()
                }
                return
            }
            // Handle other permissions if needed
        }
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = data?.data
            imageUri?.let {
                processGalleryImage(it)
            }
        }
    }

    private fun processGalleryImage(imageUri: Uri) {
        try {

            val inputImage = InputImage.fromFilePath(requireContext(), imageUri)
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        handleBarcodeResult(barcode)
                    }
                    if(barcodes.size == 0){
                        Toast.makeText(context, "Barcode not found.", Toast.LENGTH_SHORT).show()
                        startCamera()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to scan barcode from image.", Toast.LENGTH_SHORT).show()
                    startCamera()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}

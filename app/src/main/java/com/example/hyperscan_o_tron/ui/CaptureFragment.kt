package com.example.hyperscan_o_tron.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hyperscan_o_tron.data.AppDatabase
import com.example.hyperscan_o_tron.data.Product
import com.example.hyperscan_o_tron.databinding.FragmentCaptureBinding
import com.example.hyperscan_o_tron.utils.FileUtils
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CaptureFragment : Fragment() {

    private var _binding: FragmentCaptureBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageCapture: ImageCapture

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var upcCode: String

    companion object {
        private const val TAG = "CaptureFragment"
        private const val REQUEST_CODE_PERMISSIONS = 20
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            upcCode = CaptureFragmentArgs.fromBundle(it).upcCode
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.captureShelfTagButton.setOnClickListener {
            takePhoto("shelf_tag.jpg")
        }

        binding.captureFrontButton.setOnClickListener {
            takePhoto("front.jpg")
        }

        binding.captureBackButton.setOnClickListener {
            takePhoto("back.jpg")
        }

        binding.finishButton.setOnClickListener {
            // Save product data to the database
            saveProductData()
            // Navigate back to the scanner or product list

        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder()
                .build()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto(fileName: String) {
        // Create output directory and file
        val photoUri = FileUtils.createFile(requireContext(), upcCode, fileName)

        if (photoUri == null) {
            Log.e(TAG, "Failed to create file URI")
            return
        }

        // Open an OutputStream from the Uri
        val outputStream = requireContext().contentResolver.openOutputStream(photoUri)
        if (outputStream == null) {
            Log.e(TAG, "Failed to open output stream for Uri: $photoUri")
            return
        }

        // Use File-based OutputFileOptions instead of ContentResolver-based
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputStream).build()

        // Take the picture
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    lifecycleScope.launch {
                        // Optionally compress the image after capture
                        // ImageUtils.compressImage(requireContext(), Uri.fromFile(photoFile))

                        // Update UI with thumbnail or confirmation
                        updateThumbnail(fileName, photoUri)

                        Log.d(TAG, "Photo saved: $photoUri")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun saveProductData() {
        val shelfTagUri = FileUtils.getFilePath(requireContext(), upcCode, "shelf_tag.jpg")
        val frontImageUri = FileUtils.getFilePath(requireContext(), upcCode, "front.jpg")
        val backImageUri = FileUtils.getFilePath(requireContext(), upcCode, "back.jpg")

        val product = Product(
            upcCode = upcCode,
            timestamp = System.currentTimeMillis(),
            shelfTagPath = shelfTagUri?.toString(),
            frontImagePath = frontImageUri?.toString(),
            backImagePath = backImageUri?.toString()
        )

        val db = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            db.productDao().insertProduct(product)
            // Navigate back to ScannerFragment or ProductListFragment
            activity?.runOnUiThread {
                val action = CaptureFragmentDirections.actionCaptureToScanner()
                findNavController().navigate(action)
            }
        }
    }

    private fun updateThumbnail(fileName: String, photoUri: Uri) {
        when (fileName) {
            "shelf_tag.jpg" -> binding.shelfTagThumbnail.setImageURI(photoUri)
            "front.jpg" -> binding.frontThumbnail.setImageURI(photoUri)
            "back.jpg" -> binding.backThumbnail.setImageURI(photoUri)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // Permission not granted
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
package com.example.hyperscan_o_tron.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hyperscan_o_tron.data.Product
import com.example.hyperscan_o_tron.data.Scan
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

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var upcCode: String
    private var scanId: Long = 0
    private lateinit var product: Product

    companion object {
        private const val TAG = "CaptureFragment"
        private const val REQUEST_CODE_PERMISSIONS = 20
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted, start the camera
                startCamera()
            } else {
                // Permission is denied, show a message to the user
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val args = CaptureFragmentArgs.fromBundle(it)
            scanId = args.scanId
            upcCode = args.upcCode
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
        lifecycleScope.launch {
            val scan = mainViewModel.getScanByIdSync(scanId)
            if (scan != null) {
                setupCapture(scan)
            } else {
                Log.e(ContentValues.TAG, "Scan not found with ID: $scanId")
            }
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera() // Start camera directly if permission is granted
        } else {
            // Request permission using the new API
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupCapture(scan: Scan) {
        product = Product(
            upcCode = upcCode,
            scanId = scanId,
            timestamp = System.currentTimeMillis(),
            shelfTagUri = null,
            frontImageUri = null,
            backImageUri = null
        )

        binding.captureShelfTagButton.setOnClickListener {
            takePhoto(scan, "shelf_tag.jpg") { uri ->
                product.shelfTagUri = uri.toString()
                Log.d(TAG, "Shelf-tag image saved at: $uri")
            }
        }

        binding.captureFrontButton.setOnClickListener {
            takePhoto(scan, "front.jpg") { uri ->
                product.frontImageUri = uri.toString()
                Log.d(TAG, "Front image saved at: $uri")
            }
        }

        binding.captureBackButton.setOnClickListener {
            takePhoto(scan, "back.jpg") { uri ->
                product.backImageUri = uri.toString()
                Log.d(TAG, "Back image saved at: $uri")
            }
        }

        binding.finishButton.setOnClickListener {
            saveProductData()
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

    private fun takePhoto(scan: Scan, fileName: String, onImageSaved: (Uri) -> Unit) {
        val scanFolder = FileUtils.createScanFolder(requireContext(), scan.id) ?: return
        val photoUri = FileUtils.createImageFile(requireContext(), scanFolder, upcCode, fileName)

        if (photoUri == null) {
            Log.e(TAG, "Failed to create file URI")
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver.openOutputStream(photoUri)!!
        ).build()

        // Take the picture
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    lifecycleScope.launch {
                        updateThumbnail(fileName, photoUri)
                        onImageSaved(photoUri)
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
        lifecycleScope.launch {
            mainViewModel.insertProduct(product)
            Toast.makeText(requireContext(), "Product saved", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun updateThumbnail(fileName: String, photoUri: Uri) {
        when (fileName) {
            "shelf_tag.jpg" -> binding.shelfTagThumbnail.setImageURI(photoUri)
            "front.jpg" -> binding.frontThumbnail.setImageURI(photoUri)
            "back.jpg" -> binding.backThumbnail.setImageURI(photoUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
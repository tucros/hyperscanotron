package com.example.hyperscan_o_tron.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.File
import java.io.FileOutputStream
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
        )

        binding.captureShelfTagButton.setOnClickListener {
            takePhoto(scan, "shelf_tag.jpg") { uri, thumbnailUri ->
                binding.shelfTagThumbnail.setImageURI(thumbnailUri)
                product.shelfTagUri = uri.toString()
                product.shelfTagThumbnailUri = thumbnailUri.toString()
                Log.d(TAG, "Shelf-tag image saved at: $uri")
            }
        }

        binding.captureFrontButton.setOnClickListener {
            takePhoto(scan, "front.jpg") { uri, thumbnailUri ->
                binding.frontThumbnail.setImageURI(thumbnailUri)
                product.frontImageUri = uri.toString()
                product.frontImageThumbnailUri = thumbnailUri.toString()
                Log.d(TAG, "Front image saved at: $uri")
            }
        }

        binding.captureBackButton.setOnClickListener {
            takePhoto(scan, "back.jpg") { uri, thumbnailUri ->
                binding.backThumbnail.setImageURI(thumbnailUri)
                product.backImageUri = uri.toString()
                product.backImageThumbnailUri = thumbnailUri.toString()
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

    private fun takePhoto(scan: Scan, fileName: String, onImageSaved: (Uri, Uri) -> Unit) {
        val photoFileName = "${upcCode}_$fileName"
        val photoUri = FileUtils.createImageFile(requireContext(), scan.id, photoFileName)

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
                        val thumbnailUri = createThumbnail(photoUri, photoFileName)
                        onImageSaved(photoUri, thumbnailUri!!)
                        Log.d(TAG, "Photo saved: $photoUri, Thumbnail saved: $thumbnailUri")
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

    private fun createThumbnail(photoUri: Uri, fileName: String): Uri? {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(photoUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val thumbnail = Bitmap.createScaledBitmap(bitmap, 150, 150, true)
            val thumbnailFile = File(requireContext().filesDir, "thumbnail_$fileName")
            val fos = FileOutputStream(thumbnailFile)

            thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            fos.close()

            return Uri.fromFile(thumbnailFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create thumbnail: ${e.message}", e)
            return null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
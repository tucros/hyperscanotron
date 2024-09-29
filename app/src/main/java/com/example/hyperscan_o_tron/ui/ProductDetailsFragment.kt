package com.example.hyperscan_o_tron.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hyperscan_o_tron.data.Product
import com.example.hyperscan_o_tron.databinding.FragmentProductDetailsBinding

class ProductDetailsFragment : Fragment() {

    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = arguments?.getLong("productId") ?: return

        mainViewModel.getProductById(productId).observe(viewLifecycleOwner) { product ->
            if (product != null) {
                displayProductDetails(product)
            } else {
                Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayProductDetails(product: Product) {
        binding.upcTextView.text = "UPC: ${product.upcCode}"

        // Load the front image thumbnail
//        loadImageIntoView(product.frontImagePath, binding.frontImageThumbnail)

        // Load the shelf-tag image thumbnail
//        loadImageIntoView(product.shelfTagPath, binding.shelfTagImageThumbnail)

        // Load the back image thumbnail (if it exists)
        if (product.backImagePath != null) {
            binding.backImageThumbnail.visibility = View.VISIBLE
            binding.retakeBackImageButton.visibility = View.VISIBLE
//            loadImageIntoView(product.backImagePath, binding.backImageThumbnail)
        } else {
            binding.backImageThumbnail.visibility = View.GONE
            binding.retakeBackImageButton.visibility = View.GONE
        }

        // Set up retake buttons
        binding.retakeFrontImageButton.setOnClickListener {
            // Trigger front image retake action (could start a camera fragment)
            Toast.makeText(requireContext(), "Retake Front Image", Toast.LENGTH_SHORT).show()
        }

        binding.retakeShelfTagImageButton.setOnClickListener {
            // Trigger shelf-tag image retake action
            Toast.makeText(requireContext(), "Retake Shelf-Tag Image", Toast.LENGTH_SHORT).show()
        }

        binding.retakeBackImageButton.setOnClickListener {
            // Trigger back image retake action
            Toast.makeText(requireContext(), "Retake Back Image", Toast.LENGTH_SHORT).show()
        }

        // Set up image click listeners for full-size view
        binding.frontImageThumbnail.setOnClickListener {
            showFullSizeImage(product.frontImagePath)
        }

        binding.shelfTagImageThumbnail.setOnClickListener {
            showFullSizeImage(product.shelfTagPath)
        }

        binding.backImageThumbnail.setOnClickListener {
            showFullSizeImage(product.backImagePath)
        }
    }

//    private fun loadImageIntoView(imagePath: String?, imageView: ImageView) {
//        // Load the image using a utility function (e.g., using Glide or Picasso)
//        if (imagePath != null) {
//            Glide.with(this).load(imagePath).into(imageView)
//        }
//    }

    private fun showFullSizeImage(imagePath: String?) {
        if (imagePath != null) {
            // Implement full-size image view (e.g., navigate to another fragment or open a dialog)
            Toast.makeText(requireContext(), "Showing full-size image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.hyperscan_o_tron.ui

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hyperscan_o_tron.data.Scan
import com.example.hyperscan_o_tron.databinding.FragmentScanDetailsBinding
import kotlinx.coroutines.launch

class ScanDetailsFragment : Fragment() {

    private var _binding: FragmentScanDetailsBinding? = null
    private val binding get() = _binding!!

    // Get navigation arguments (scanId)
    private val args: ScanDetailsFragmentArgs by navArgs()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scanId = args.scanId

        lifecycleScope.launch {
            val scan = mainViewModel.getScanByIdSync(scanId)
            if (scan != null) {
                //log scan id, name, folder
                Log.d(TAG, "Scan ID: $scanId, Name: ${scan.name}, Folder: ${scan.folderPath}")
                displayScanDetails(scan)
                loadProductsForScan(scan.id)
            } else {
                Log.e(TAG, "Scan not found with ID: $scanId")
            }
        }

        // Set up RecyclerView for products
        binding.productRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Add product button listener (navigate to product scan workflow)
        binding.addProductFab.setOnClickListener {
            // Navigate to ProductScanFragment (add product)
            findNavController().navigate(
                ScanDetailsFragmentDirections.actionScanDetailsToScanner(scanId)
            )
        }

        // Delete scan button listener
        binding.deleteScanButton.setOnClickListener {
            deleteScan(scanId)
        }
    }

    // Display scan details in the UI
    private fun displayScanDetails(scan: Scan) {
        binding.scanNameTextView.text = scan.name
        val createdDate = scan.createdAt.toString()
        binding.scanDatesTextView.text = "Created: $createdDate"
    }

    // Load the products associated with the scan
    private fun loadProductsForScan(scanId: Long) {
        mainViewModel.getProductsForScan(scanId).observe(viewLifecycleOwner, Observer { products ->
            binding.productRecyclerView.adapter = ProductAdapter(products) { product ->
                findNavController().navigate(
                    ScanDetailsFragmentDirections.actionScanDetailsToProductDetails(product.upcCode)
                )
            }
        })
    }

    // Delete the scan and navigate back to the MainFragment
    private fun deleteScan(scanId: Long) {
        mainViewModel.deleteScan(scanId)
        findNavController().navigateUp()
        Toast.makeText(requireContext(), "Scan deleted", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}

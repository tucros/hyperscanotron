package com.example.hyperscan_o_tron.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hyperscan_o_tron.data.Scan
import com.example.hyperscan_o_tron.databinding.FragmentScanDetailsBinding

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

        // Get the scanId from the arguments
        val scanId = args.scanId

        // Observe the scan and update the UI
        mainViewModel.getScanById(scanId).observe(viewLifecycleOwner, Observer { scan ->
            if (scan != null) {
                displayScanDetails(scan)
                loadProductsForScan(scan.id)
            }
        })

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
        val createdDate = scan.createdAt.toString() // Format as needed
        val modifiedDate = scan.modifiedAt.toString()
        binding.scanDatesTextView.text = "Created: $createdDate • Modified: $modifiedDate"
    }

    // Load the products associated with the scan
    private fun loadProductsForScan(scanId: Long) {
        mainViewModel.getProductsForScan(scanId).observe(viewLifecycleOwner, Observer { products ->
            binding.productRecyclerView.adapter = ProductAdapter(products)
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

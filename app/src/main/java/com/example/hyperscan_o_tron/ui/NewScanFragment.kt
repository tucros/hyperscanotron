package com.example.hyperscan_o_tron.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hyperscan_o_tron.data.Scan
import com.example.hyperscan_o_tron.databinding.FragmentNewScanBinding
import com.example.hyperscan_o_tron.utils.FileUtils
import java.util.Date

class NewScanFragment : Fragment() {

    private var _binding: FragmentNewScanBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = FragmentNewScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listeners for the buttons
        binding.startButton.setOnClickListener {
            val scanName = binding.scanNameEditText.text.toString()
            if (scanName.isNotEmpty()) {
                createNewScan(scanName)
            } else {
                Toast.makeText(requireContext(), "Please enter a scan name", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp() // Navigate back to the previous fragment (MainFragment)
        }
    }

    private fun createNewScan(scanName: String) {
        val currentTime = Date()
        val newScan = Scan(
            name = scanName,
            createdAt = currentTime,
            modifiedAt = currentTime,
            folderPath = null
        )

        // Insert the scan into the database via ViewModel
        mainViewModel.createScan(newScan) { scanId ->
            val scanFolderPath = createScanFolder(scanId)

            if (scanFolderPath != null) {
                mainViewModel.updateScanFolder(scanId, scanFolderPath)
                findNavController().navigate(
                    NewScanFragmentDirections.actionNewScanToScanDetails(scanId)
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error creating folder for scan",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun createScanFolder(scanId: Long): String? {
        val scanFolder = FileUtils.createScanFolder(requireContext(), scanId)
        return if (scanFolder != null && scanFolder.exists()) {
            scanFolder.absolutePath // Return the folder path
        } else {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}

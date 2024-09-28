package com.example.hyperscan_o_tron.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hyperscan_o_tron.R

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        // Get reference to the start scanning button
        val startScanningButton = view.findViewById<Button>(R.id.start_scanning_button)

        // Set click listener for the button to navigate to ScannerFragment
        startScanningButton.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_scanner)
        }

        return view
    }
}

package com.example.hyperscan_o_tron.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hyperscan_o_tron.R
import com.example.hyperscan_o_tron.data.Scan
import com.example.hyperscan_o_tron.databinding.ItemScanBinding

class ScanAdapter(
    private val scans: List<Scan>,
    private val onItemClick: (Scan) -> Unit
) : RecyclerView.Adapter<ScanAdapter.ScanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        // Using ViewBinding to inflate the item layout
        val binding = ItemScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        holder.bind(scans[position], onItemClick)
    }

    override fun getItemCount() = scans.size

    class ScanViewHolder(private val binding: ItemScanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(scan: Scan, onItemClick: (Scan) -> Unit) {
            // Bind data using ViewBinding
            val context = binding.root.context
            binding.scanName.text = scan.name
            binding.scanCreated.text = context.getString(R.string.scan_created, scan.createdAt)
            binding.scanModified.text = context.getString(R.string.scan_modified, scan.modifiedAt)
            binding.numProducts.text =
                context.getString(R.string.scan_products) // Placeholder for product count, adjust as needed

            // Set the click listener
            binding.root.setOnClickListener {
                onItemClick(scan)
            }
        }
    }
}
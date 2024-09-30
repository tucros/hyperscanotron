package com.example.hyperscan_o_tron.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.hyperscan_o_tron.data.Product
import com.example.hyperscan_o_tron.databinding.ItemProductBinding

class ProductAdapter(
    private val products: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product, onProductClick) // Bind the click listener to the product
    }

    override fun getItemCount() = products.size

    class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product, onProductClick: (Product) -> Unit) {
            // Bind data to the view
            binding.productNameTextView.text = product.upcCode

            // Set click listener on the root view of the item
            binding.root.setOnClickListener {
                onProductClick(product) // Trigger the lambda on product click
            }

            //log the image path
            println("Product image path: ${product.frontImageUri}")
            loadImageIntoView(product.frontImageUri, binding.productThumbnailImageView)
        }

        private fun loadImageIntoView(imagePath: String?, imageView: ImageView) {
            imagePath?.let {
                val imageUri = Uri.parse(imagePath)
                imageView.setImageURI(imageUri)
            }
        }
    }
}

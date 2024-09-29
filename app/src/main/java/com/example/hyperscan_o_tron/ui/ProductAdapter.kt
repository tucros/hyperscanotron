package com.example.hyperscan_o_tron.ui

import android.view.LayoutInflater
import android.view.ViewGroup
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

            // Load image into the ImageView (assuming a method loadImageIntoView exists)
//            loadImageIntoView(product.frontImagePath, binding.productThumbnailImageView)
        }

//        private fun loadImageIntoView(imagePath: String?, imageView: ImageView) {
        // Use Glide, Picasso, or another library to load the image
//            if (imagePath != null) {
//                Glide.with(imageView.context).load(imagePath).into(imageView)
//            }
//        }
    }
}

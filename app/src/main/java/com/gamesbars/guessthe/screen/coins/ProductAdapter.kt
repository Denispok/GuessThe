package com.gamesbars.guessthe.screen.coins

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gamesbars.guessthe.databinding.ItemProductBinding

class ProductAdapter : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductItemCallback()) {

    var onItemClickListener: ((Product) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.setOnClickListener {
            (binding.root.tag as? Product)?.also { product ->
                onItemClickListener?.invoke(product)
            }
        }
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Product) {
            itemView.tag = item
            binding.coinsTv.text = item.coins.toString()
            binding.priceTv.text = item.price
            binding.removeAdsTv.isVisible = item.isAdsTitleVisible
        }
    }

    class ProductItemCallback : DiffUtil.ItemCallback<Product>() {

        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return areItemsTheSame(oldItem, newItem)
        }
    }
}
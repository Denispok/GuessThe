package com.gamesbars.guessthe.screen.coins

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gamesbars.guessthe.R
import kotlinx.android.synthetic.main.item_product.view.*

class ProductAdapter : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductItemCallback()) {

    var onItemClickListener: ((Product) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        view.setOnClickListener {
            (view.tag as? Product)?.also { product ->
                onItemClickListener?.invoke(product)
            }
        }
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Product) {
            itemView.apply {
                tag = item
                coinsTv.text = item.coins.toString()
                priceTv.text = item.price
                removeAdsTv.isVisible = item.isAdsTitleVisible
            }
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
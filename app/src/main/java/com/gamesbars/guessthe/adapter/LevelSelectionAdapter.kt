package com.gamesbars.guessthe.adapter

import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage.getDrawableResIdByName
import com.gamesbars.guessthe.Storage.getLevelName
import com.gamesbars.guessthe.Storage.getPackSize
import com.gamesbars.guessthe.customview.LevelView

class LevelSelectionAdapter(
    private val pack: String,
    private val completedLevelCount: Int
) : RecyclerView.Adapter<LevelSelectionAdapter.LevelSelectionViewHolder>() {

    var onItemClickListener: ((level: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelSelectionViewHolder {
        val view = LevelView(parent.context)
        view.setOnClickListener { onItemClickListener?.invoke(view.tag as Int) }
        return LevelSelectionViewHolder(view)
    }

    override fun getItemCount(): Int = getPackSize(pack)

    override fun onBindViewHolder(viewHolder: LevelSelectionViewHolder, position: Int) {
        val number = position + 1
        viewHolder.itemView.tag = number

        if (number <= completedLevelCount + 1) {
            Glide.with(viewHolder.itemView)
                .load(getDrawableResIdByName(getLevelName(pack, number)))
                .transition(withCrossFade())
                .into(viewHolder.image)
        } else {
            viewHolder.image.setImageDrawable(null)
        }

        viewHolder.number.text = number.toString()
    }

    class LevelSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageIv)
        val number: TextView = itemView.findViewById(R.id.titleTv)

        init {
            image.setColorFilter(0x66000000.toInt(), PorterDuff.Mode.DARKEN)
        }
    }
}

package com.gamesbars.guessthe.adapter

import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.gamesbars.guessthe.LevelMenuActivity
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage.getDrawableResIdByName
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

    override fun getItemCount(): Int = LevelMenuActivity.PACK_LEVELS_COUNT

    override fun onBindViewHolder(viewHolder: LevelSelectionViewHolder, position: Int) {
        val number = position + 1
        viewHolder.itemView.tag = number

        if (number <= completedLevelCount + 1) {
            viewHolder.image.setImageResource(getDrawableResIdByName(pack + number + "thum"))
            viewHolder.image.setColorFilter(0x66000000.toInt(), PorterDuff.Mode.DARKEN)
            viewHolder.image.setBackgroundResource(R.color.colorLevelSelectionItemBack)
        } else {
            viewHolder.image.setImageDrawable(null)
            viewHolder.image.setBackgroundResource(R.color.colorLevelSelectionItemBackDark)
        }

        viewHolder.number.text = number.toString()
    }

    class LevelSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.item_levelselection_image)
        val number: TextView = itemView.findViewById(R.id.item_levelselection_number)
    }
}

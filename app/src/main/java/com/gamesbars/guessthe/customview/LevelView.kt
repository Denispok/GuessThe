package com.gamesbars.guessthe.customview

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.gamesbars.guessthe.R

class LevelView(context: Context) : FrameLayout(context) {

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.item_levelselection, this, true)
        val padding = context.resources.getDimension(R.dimen.item_levelselection_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}

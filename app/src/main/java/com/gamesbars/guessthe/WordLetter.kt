package com.gamesbars.guessthe

import android.content.Context
import android.graphics.Color
import android.support.v4.content.res.ResourcesCompat
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView

class WordLetter(context: Context, val isSpace: Boolean = false) : TextView(context) {

    init {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.letter_text_size))
        setTextColor(Color.BLACK)

        val dimension = resources.getDimension(R.dimen.letter_size).toInt()
        val params = LinearLayout.LayoutParams(dimension, dimension)
        val margins = resources.getDimension(R.dimen.letter_margins).toInt()
        params.setMargins(margins, margins, margins, margins)
        layoutParams = params

        if (!isSpace) background = ResourcesCompat.getDrawable(resources, R.drawable.letter_selector, null)
    }

}
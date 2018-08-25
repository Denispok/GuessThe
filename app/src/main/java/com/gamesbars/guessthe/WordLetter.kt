package com.gamesbars.guessthe

import android.content.Context
import android.graphics.Color
import android.support.v4.content.res.ResourcesCompat
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class WordLetter(context: Context, val isSpace: Boolean = false) : TextView(context) {

    var letter: Letter? = null
    val dimension: Int

    init {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.word_letter_text_size))
        setTextColor(Color.WHITE)

        dimension = resources.getDimension(R.dimen.word_letter_size).toInt()
        val params = LinearLayout.LayoutParams(dimension, dimension)
        val margins = resources.getDimension(R.dimen.word_letter_margins).toInt()
        params.setMargins(margins, margins, margins, margins)
        layoutParams = params
        gravity = Gravity.CENTER
        val padding = resources.getDimension(R.dimen.word_padding).toInt()
        setPadding(0, 0, padding, padding)
        isClickable = false

        if (!isSpace) background = ResourcesCompat.getDrawable(resources, R.drawable.word_letter_selector, null)
    }

}

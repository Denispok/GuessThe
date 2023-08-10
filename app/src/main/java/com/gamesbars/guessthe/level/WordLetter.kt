package com.gamesbars.guessthe.level

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.gamesbars.guessthe.R

class WordLetter(
    /** Known chars always visible and without background selector */
    val knownChar: Char? = null,
    context: Context
) : TextView(context) {

    var letter: Letter? = null
    val dimension: Int

    init {
        typeface = ResourcesCompat.getFont(context, R.font.exo2_bold)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.word_letter_text_size))
        setTextColor(Color.WHITE)

        dimension = resources.getDimension(R.dimen.word_letter_size).toInt()
        val params = LinearLayout.LayoutParams(dimension, dimension)
        val margins = resources.getDimension(R.dimen.word_letter_margins).toInt()
        params.setMargins(margins, margins, margins, margins)
        layoutParams = params
        gravity = Gravity.CENTER
        val padding = resources.getDimension(R.dimen.word_letter_padding).toInt()
        setPadding(0, 0, padding, padding)
        isClickable = false

        if (knownChar == null) {
            background = ResourcesCompat.getDrawable(resources, R.drawable.word_letter_selector, null)
        } else {
            text = knownChar.toString()
        }
    }
}

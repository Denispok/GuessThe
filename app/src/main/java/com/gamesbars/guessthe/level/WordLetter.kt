package com.gamesbars.guessthe.level

import android.content.Context
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.gamesbars.guessthe.R
import io.github.inflationx.calligraphy3.TypefaceUtils

class WordLetter(context: Context, val isSpace: Boolean = false, val knownChar: Char? = null) : TextView(context) {

    var letter: Letter? = null
    val dimension: Int

    init {
        typeface = TypefaceUtils.load(resources.assets, "fonts/Exo_2/Exo2-Bold.ttf")
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

        if (!isSpace && knownChar == null) {
            background = ResourcesCompat.getDrawable(resources, R.drawable.word_letter_selector, null)
        }

        if (knownChar != null) {
            setText(knownChar.toString())
        }
    }

}

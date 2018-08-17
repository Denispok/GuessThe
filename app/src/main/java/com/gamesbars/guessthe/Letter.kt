package com.gamesbars.guessthe

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.support.v4.content.res.ResourcesCompat
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class Letter(context: Context, val letterID: Int, val letter: Char) : TextView(context) {

    var isChosen: Boolean = false

    init {
        text = letter.toString()
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.letter_text_size))
        setTextColor(Color.BLACK)

        val dimension = resources.getDimension(R.dimen.letter_size).toInt()
        val params = LinearLayout.LayoutParams(dimension, dimension)
        val margins = resources.getDimension(R.dimen.letter_margins).toInt()
        params.setMargins(margins, margins, margins, margins)
        layoutParams = params
        gravity = Gravity.CENTER
        val padding = resources.getDimension(R.dimen.letter_padding).toInt()
        setPadding(0, 0, padding, padding)

        pivotX = 0f
        pivotY = 0f

        background = ResourcesCompat.getDrawable(resources, R.drawable.letter_selector, null)
    }

    fun chooseIn(wordLetter: WordLetter, animationDuration: Long) {
        isChosen = true
        this.isClickable = false
        // animation starting
        val wordLetterPosition = intArrayOf(0, 0)
        val letterPosition = intArrayOf(0, 0)
        wordLetter.getLocationInWindow(wordLetterPosition)
        this.getLocationOnScreen(letterPosition)
        val animationX = ObjectAnimator.ofFloat(this, "translationX", (wordLetterPosition[0] - letterPosition[0]).toFloat())
        val animationY = ObjectAnimator.ofFloat(this, "translationY", (wordLetterPosition[1] - letterPosition[1]).toFloat())
        val scale = wordLetter.width.toFloat() / width
        val animationScaleX = ObjectAnimator.ofFloat(this, "scaleX", scale)
        val animationScaleY = ObjectAnimator.ofFloat(this, "scaleY", scale)

        val set = AnimatorSet()
        set.play(animationX).with(animationY).with(animationScaleX).with(animationScaleY)
        set.duration = animationDuration
        set.start()
    }

    fun chooseOut(animationDuration: Long) {
        isChosen = false
        Handler().postDelayed({ this.isClickable = true }, animationDuration)
        // animation starting
        val animationX = ObjectAnimator.ofFloat(this, "translationX", 0f)
        val animationY = ObjectAnimator.ofFloat(this, "translationY", 0f)
        val animationScaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f)
        val animationScaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f)

        val set = AnimatorSet()
        set.play(animationX).with(animationY).with(animationScaleX).with(animationScaleY)
        set.duration = animationDuration
        set.start()
    }
}
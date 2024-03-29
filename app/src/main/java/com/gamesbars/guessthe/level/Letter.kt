package com.gamesbars.guessthe.level

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.gamesbars.guessthe.R

class Letter(context: Context, val letterId: Int, val letter: Char, val wordLetterId: Int? = null, var isTipGuessed: Boolean = false) : TextView(context) {

    var currentWordLetter: WordLetter? = null
    val dimension: Int
    val margin: Int

    init {
        text = letter.toString()
        typeface = ResourcesCompat.getFont(context, R.font.exo2_bold)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.letter_text_size))
        setTextColor(Color.BLACK)

        dimension = resources.getDimension(R.dimen.letter_size).toInt()
        margin = resources.getDimension(R.dimen.letter_margins).toInt()
        val params = LinearLayout.LayoutParams(dimension, dimension)
        params.setMargins(margin, margin, margin, margin)
        layoutParams = params

        gravity = Gravity.CENTER
        val padding = resources.getDimension(R.dimen.letter_padding).toInt()
        setPadding(0, 0, padding, padding)

        pivotX = 0f
        pivotY = 0f

        background = ResourcesCompat.getDrawable(resources, R.drawable.button_selector_white, null)
    }

    fun chooseIn(wordLetter: WordLetter, animationDuration: Long) {
        currentWordLetter = wordLetter
        this.isClickable = false

        // animation starting
        val wordLetterPosition = intArrayOf(0, 0)
        val letterPosition = intArrayOf(0, 0)
        wordLetter.getLocationInWindow(wordLetterPosition)
        this.getLocationOnScreen(letterPosition)
        val animationX = ObjectAnimator.ofFloat(this, "translationX", (wordLetterPosition[0] - letterPosition[0]).toFloat())
        val animationY = ObjectAnimator.ofFloat(this, "translationY", (wordLetterPosition[1] - letterPosition[1]).toFloat())
        val scaleX = wordLetter.width.toFloat() / width
        val scaleY = wordLetter.height.toFloat() / height
        val animationScaleX = ObjectAnimator.ofFloat(this, "scaleX", scaleX)
        val animationScaleY = ObjectAnimator.ofFloat(this, "scaleY", scaleY)
        val animationAlpha = ObjectAnimator.ofFloat(this, "alpha", 0f)
        animationAlpha.interpolator = AccelerateInterpolator(3f)

        val set = AnimatorSet()
        set.play(animationX).with(animationY).with(animationScaleX).with(animationScaleY).with(animationAlpha)
        set.duration = animationDuration
        set.start()
    }

    fun chooseOut(animationDuration: Long) {
        Handler().postDelayed({
            currentWordLetter = null
            this.isClickable = true
        }, animationDuration)

        // animation starting
        val animationX = ObjectAnimator.ofFloat(this, "translationX", 0f)
        val animationY = ObjectAnimator.ofFloat(this, "translationY", 0f)
        val animationScaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f)
        val animationScaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f)
        val animationAlpha = ObjectAnimator.ofFloat(this, "alpha", 1f)
        animationAlpha.interpolator = DecelerateInterpolator(3f)

        val set = AnimatorSet()
        set.play(animationX).with(animationY).with(animationScaleX).with(animationScaleY).with(animationAlpha)
        set.duration = animationDuration
        set.start()
    }

    fun gone(animationDuration: Long) {
        Handler().postDelayed({
            this.visibility = View.INVISIBLE
            this.isClickable = false
        }, animationDuration)

        val animationY = ObjectAnimator.ofFloat(this, "translationY", this.translationY + 20)
        val animationAlpha = ObjectAnimator.ofFloat(this, "alpha", 0f)

        val set = AnimatorSet()
        set.play(animationY).with(animationAlpha)
        set.duration = animationDuration
        set.start()
    }
}

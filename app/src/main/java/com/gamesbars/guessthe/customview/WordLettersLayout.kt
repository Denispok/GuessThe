package com.gamesbars.guessthe.customview

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import com.gamesbars.guessthe.level.WordLetter

class WordLettersLayout(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    companion object {
        const val maxLettersPerLine = 16
    }

    private val linearList = mutableListOf<LinearLayout>()
    private var wordLetters: List<WordLetter>? = null

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (wordLetters != null) {
            val oldParams = wordLetters!![0].layoutParams as LayoutParams
            var minSize = wordLetters!![0].width + oldParams.leftMargin + oldParams.rightMargin

            for (linear in linearList) {
                val size = linear.width / linear.childCount
                if (size < minSize) minSize = size
            }

            val params = LayoutParams(minSize, wordLetters!![0].height)
            wordLetters!!.forEach {
                val oldLetterParams = it.layoutParams as LayoutParams
                val oldLetterWidth = oldLetterParams.width + oldLetterParams.leftMargin + oldLetterParams.rightMargin
                if (oldLetterWidth != params.width) it.layoutParams = params
            }
        }
    }

    fun setWordLetters(list: List<WordLetter>) {
        wordLetters = list

        var currentLinear = inflateLinear()
        var currentLineSize = 0
        var currentWordSize = -1
        var currentSpacePosition = -1

        for (i in 0 until list.size) {
            if (list[i].isSpace) {
                currentSpacePosition = -1
                currentWordSize = -1
            }

            if (currentSpacePosition == -1) {
                for (k in i + 1 until list.size) {
                    if (list[k].isSpace) {
                        currentSpacePosition = k
                        break
                    }
                }
                if (currentSpacePosition == -1) currentSpacePosition = list.size
            }

            if (currentWordSize == -1) {
                currentWordSize = currentSpacePosition - i
                if (currentWordSize > maxLettersPerLine && currentLineSize == 0) {
                    // do nothing
                } else if (currentWordSize + currentLineSize > maxLettersPerLine) {
                    currentLinear = inflateLinear()
                    currentLineSize = 0
                }
            }

            if (!(list[i].isSpace && currentLineSize == 0)) {
                currentLinear.addView(list[i])
                currentLineSize++
            }
        }
    }

    private fun inflateLinear(): LinearLayout {
        val linear = LinearLayout(context)
        linear.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        linear.gravity = Gravity.CENTER
        linear.orientation = HORIZONTAL
        this.addView(linear)
        linearList.add(linear)
        return linear
    }

    private val WordLetter.isSpace get() = knownChar == ' '
}
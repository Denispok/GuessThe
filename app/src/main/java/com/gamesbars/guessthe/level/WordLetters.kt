package com.gamesbars.guessthe.level

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Handler
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.fragment.LevelFragment
import com.gamesbars.guessthe.playSound

class WordLetters(private val fragment: LevelFragment, private val word: String) {
    private lateinit var wordLayout: LinearLayout
    private val wordLetters = arrayListOf<WordLetter>()
    val animationDuration = 500L

    init {
        for (char in word) {
            wordLetters.add(WordLetter(fragment.context!!, char == ' '))
        }
        for (wordLetter in wordLetters) {
            wordLetter.setOnClickListener {
                it as WordLetter
                if (it.letter != null) removeOutLetter(it)
            }
        }
    }

    private fun playIsFullAnimation() {
        playSound(fragment.context!!, R.raw.isfull)
        val animator = ObjectAnimator.ofFloat(wordLayout, "translationX", 0f, -14f, 14f, -14f, 14f, -14f, 14f, -14f, 14f, 0f)
        animator.interpolator = LinearInterpolator()
        animator.duration = 700
        animator.start()
    }

    private fun checkWord() {
        var userWord = ""
        for (wordLetter in wordLetters) {
            if (wordLetter.isSpace) {
                userWord += " "
                continue
            }
            if (wordLetter.letter == null) return
            userWord += wordLetter.text
        }
        if (userWord == word) fragment.win()
        else {
            playIsFullAnimation()
        }
    }

    private fun freeWordLetter(): WordLetter? {
        for (wordLetter in wordLetters) {
            if (wordLetter.isSpace) continue
            if (wordLetter.letter == null) return wordLetter
        }
        return null
    }

    fun addLettersToLayout(linearLayout: LinearLayout) {
        if (!this::wordLayout.isInitialized) {
            wordLayout = linearLayout
            for (letter in wordLetters) {
                wordLayout.addView(letter)
            }
            Handler().postDelayed({
                if (wordLetters[0].x <= 2) {
                    for (wordLetter in wordLetters) {
                        val params = LinearLayout.LayoutParams(wordLetter.dimension, wordLetter.dimension, 1f)
                        wordLetter.layoutParams = params
                    }
                }
            }, 100)
        }
    }

    fun tipLetterGuessed(letter: Letter) {
        val wordLetter = wordLetters[letter.wordLetterId!!]
        if (wordLetter.letter != null && wordLetter.letter != letter) removeOutLetter(wordLetter)
        if (letter.currentWordLetter != null && letter.currentWordLetter != wordLetter) {
            val currentWordLetter = letter.currentWordLetter!!
            currentWordLetter.text = ""
            currentWordLetter.isClickable = false
            currentWordLetter.letter = null
        }
        letter.chooseIn(wordLetter, animationDuration)
        wordLetter.isClickable = false
        wordLetter.letter = letter
        wordLetter.letter!!.isTipGuessed = true
        Handler().postDelayed({
            wordLetter.text = letter.text
            wordLetter.isEnabled = false
            wordLetter.setTextColor(Color.parseColor("#ff7e00"))
            checkWord()
        }, animationDuration)
    }

    fun addLetter(letter: Letter) {
        val freeWordLetter = freeWordLetter()
        if (freeWordLetter == null) {
            playIsFullAnimation()
        } else {
            playSound(fragment.context!!, R.raw.choosein)
            letter.chooseIn(freeWordLetter, animationDuration)
            freeWordLetter.letter = letter
            Handler().postDelayed({
                freeWordLetter.text = letter.text
                freeWordLetter.isClickable = true
                checkWord()
            }, animationDuration)
        }
    }

    private fun removeOutLetter(wordLetter: WordLetter) {
        playSound(fragment.context!!, R.raw.chooseout)
        wordLetter.letter?.chooseOut(animationDuration)
        removeLetter(wordLetter)
    }

    fun removeLetter(wordLetter: WordLetter) {
        wordLetter.text = ""
        wordLetter.isClickable = false
        wordLetter.letter = null
    }
}

package com.gamesbars.guessthe

import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.widget.LinearLayout

class WordLetters(val fragment: LevelFragment, private val word: String) {
    private lateinit var wordLayout: LinearLayout
    private val wordLetters = arrayListOf<WordLetter>()
    private val animationDuration = 500L

    init {
        for (char in word) {
            wordLetters.add(WordLetter(fragment.context!!, char == ' '))
        }
        for (wordLetter in wordLetters) {
            wordLetter.setOnClickListener {
                it as WordLetter
                if (it.letter != null) {
                    it.text = ""
                    it.isClickable = false
                    it.letter?.chooseOut(animationDuration)
                    it.letter = null
                }
            }
        }
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
            // play isFull animation
            Log.d("MYLOG", "IS FULL!!!")
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
            }, 1)
        }
    }

    fun letterGuessed(letter: Letter) {
        val wordLetter = wordLetters[letter.wordLetterId!!]
        letter.chooseIn(wordLetter, animationDuration)
        wordLetter.isClickable = false
        wordLetter.letter = letter
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
            // play isFull animation
            Log.d("MYLOG", "IS FULL!!!")
        } else {
            letter.chooseIn(freeWordLetter, animationDuration)
            freeWordLetter.letter = letter
            Handler().postDelayed({
                freeWordLetter.text = letter.text
                freeWordLetter.isClickable = true
                checkWord()
            }, animationDuration)
        }
    }
}
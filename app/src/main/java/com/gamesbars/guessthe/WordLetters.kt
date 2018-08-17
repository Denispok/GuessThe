package com.gamesbars.guessthe

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.LinearLayout

class WordLetters(context: Context, private val word: String) {
    private lateinit var wordLayout: LinearLayout
    private val wordLetters = arrayListOf<WordLetter>()
    private val animationDuration = 500L

    init {
        for (char in word) {
            wordLetters.add(WordLetter(context, char == ' '))
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
        if (userWord == word) {
            // win
            Log.d("MYLOG", "WIN!!!")
        } else {
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
        }
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
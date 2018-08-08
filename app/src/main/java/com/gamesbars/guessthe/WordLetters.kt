package com.gamesbars.guessthe

import android.content.Context
import android.widget.LinearLayout

class WordLetters(context: Context, word: String) {
    val letters = arrayListOf<WordLetter>()
    lateinit var wordLayout: LinearLayout

    init {
        for (char in word) {
            letters.add(WordLetter(context, char == ' '))
        }
    }

    fun addLettersToLayout(linearLayout: LinearLayout) {
        if (!this::wordLayout.isInitialized) {
            wordLayout = linearLayout
            for (letter in letters) {
                wordLayout.addView(letter)
            }
        }
    }
}
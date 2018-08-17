package com.gamesbars.guessthe

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import java.util.*

class Letters(context: Context, word: String) {
    private val letterCount = 14
    private var alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
    private val letters: Array<Letter>
    private lateinit var lettersLayout1: LinearLayout
    private lateinit var lettersLayout2: LinearLayout

    init {
        val newLetters = arrayOfNulls<Letter>(letterCount)

        for (letter in word) {
            if (letter == ' ') continue
            var random: Int
            do {
                random = (0 until letterCount).random()
            } while (newLetters[random] != null)
            newLetters[random] = Letter(context, random, letter)
            alphabet = alphabet.remove(letter)
        }

        for (id in (0 until letterCount)) {
            if (newLetters[id] == null) {
                val newLetter = alphabet[(0 until alphabet.length).random()]
                alphabet = alphabet.remove(newLetter)
                newLetters[id] = Letter(context, id, newLetter)
            }
        }
        letters = newLetters.requireNoNulls()
    }

    fun addLettersToLayout(linearLayout1: LinearLayout, linearLayout2: LinearLayout) {
        if (!(this::lettersLayout1.isInitialized && this::lettersLayout2.isInitialized)) {
            lettersLayout1 = linearLayout1
            lettersLayout2 = linearLayout2
            for (i in 1..letterCount) {
                if (i <= letterCount / 2) lettersLayout1.addView(letters[i - 1])
                else lettersLayout2.addView(letters[i - 1])
            }
        }
    }

    fun setLettersOnClickListener(onClickListener: (View) -> Unit) {
        for (letter in letters) {
            letter.setOnClickListener(onClickListener)
        }
    }
}

fun ClosedRange<Int>.random() =
        Random().nextInt((endInclusive + 1) - start) + start

fun String.remove(char: Char) = this.replace(char.toString(), "")
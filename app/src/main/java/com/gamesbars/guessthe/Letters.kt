package com.gamesbars.guessthe

import android.content.Context
import android.widget.LinearLayout
import java.util.*

class Letters(context: Context, word: String, val lettersLayout1: LinearLayout, val lettersLayout2: LinearLayout) {
    val letterCount = 14
    var alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
    val letters: Array<Letter>

    init {
        val newLetters = arrayOfNulls<Letter>(letterCount)

        for (letter in word) {
            if (letter == ' ') continue
            var random: Int
            do {
                random = (0 until letterCount).random()
            } while (newLetters[random] != null)
            newLetters[random] = Letter(context, random, letter)
            alphabet.remove(letter)
        }

        for (id in (0 until letterCount)) {
            if (newLetters[id] == null) {
                val newLetter = alphabet[(0 until alphabet.length).random()]
                alphabet.remove(newLetter)
                newLetters[id] = Letter(context, id, newLetter)
            }
        }
        letters = newLetters.requireNoNulls()

        for (i in 1..letterCount) {
            if (i <= letterCount / 2) lettersLayout1.addView(letters[i - 1])
            else lettersLayout2.addView(letters[i - 1])
        }
    }
}

fun ClosedRange<Int>.random() =
        Random().nextInt((endInclusive + 1) - start) + start

fun String.remove(char: Char) = this.replace(char.toString(), "")
package com.gamesbars.guessthe

import android.content.Context
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import java.util.*

class Letters(context: Context, word: String, pack: String) {
    private val letterCount = 14
    private var alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
    private val letters: Array<Letter>
    private lateinit var lettersLayout1: LinearLayout
    private lateinit var lettersLayout2: LinearLayout

    init {
        val saves = context.getSharedPreferences("saves", Context.MODE_PRIVATE)
        val level = pack + saves.getInt(pack, 1)
        val newLetters = arrayOfNulls<Letter>(letterCount)

        if (saves.contains(level)) {
            val letterString = saves.getString(level, "")
            var letterId = 0

            for (id in 0 until letterString.length) {
                val char = letterString[id]
                if (char.isDigit()) continue
                val wordLetterId = if (id + 1 < letterString.length && letterString[id + 1].isDigit())
                    if (id + 2 < letterString.length && letterString[id + 2].isDigit()) letterString.substring(id + 1..id + 2).toInt()
                    else letterString[id + 1].toString().toInt()
                else null
                newLetters[letterId] = Letter(context, letterId, char.toLowerCase(), wordLetterId, char.isUpperCase())
                letterId++
            }
        } else {
            for (id in 0 until word.length) {
                if (word[id] == ' ') continue
                var random: Int
                do {
                    random = (0 until letterCount).random()
                } while (newLetters[random] != null)
                newLetters[random] = Letter(context, random, word[id], id)
                alphabet = alphabet.remove(word[id])
            }

            for (id in (0 until letterCount)) {
                if (newLetters[id] == null) {
                    val newLetter = alphabet[(0 until alphabet.length).random()]
                    alphabet = alphabet.remove(newLetter)
                    newLetters[id] = Letter(context, id, newLetter)
                }
            }

            val lettersString = StringBuilder()
            for (letter in newLetters.requireNoNulls()) {
                lettersString.append(letter.letter)
                if (letter.wordLetterId != null) lettersString.append(letter.wordLetterId)
            }

            val editor = saves.edit()
            editor.putString(level, lettersString.toString())
            editor.apply()
        }

        letters = newLetters.requireNoNulls()
    }

    fun showChosenLetters(wordLetters: WordLetters) {
        Handler().postDelayed({
            for (letter in letters) {
                if (letter.isChosen) wordLetters.letterGuessed(letter)
            }
        }, 500)
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

package com.gamesbars.guessthe.level

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import java.util.*

class Letters(context: Context, word: String, pack: String) {
    private val letterCount = 14
    private var alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя012345"
    private val letters: Array<Letter>
    private lateinit var lettersLayout1: LinearLayout
    private lateinit var lettersLayout2: LinearLayout
    var removeTipUsed = false

    init {
        val saves = context.getSharedPreferences("saves", Context.MODE_PRIVATE)
        val level = pack + saves.getInt(pack, 1)
        val newLetters = arrayOfNulls<Letter>(letterCount)

        if (saves.contains(level)) {
            val letterString = saves.getString(level, "")
            var letterId = 0
            var isLetterNow = false

            for (id in 0 until letterString.length) {
                val char = letterString[id]
                if (char == '!') {
                    removeTipUsed = true
                    break
                }
                if (char == '_') {
                    isLetterNow = true
                    continue
                }
                if (isLetterNow) {
                    val wordLetterId = if (id + 1 < letterString.length && letterString[id + 1].isDigit())
                        if (id + 2 < letterString.length && letterString[id + 2].isDigit()) letterString.substring(id + 1..id + 2).toInt()
                        else letterString[id + 1].toString().toInt()
                    else null
                    val isGuessed = if (id + 1 < letterString.length && letterString[id + 1] == '*') true
                        else if (id + 2 < letterString.length && letterString[id + 2] == '*') true
                        else if (id + 3 < letterString.length && letterString[id + 3] == '*') true
                        else false
                    newLetters[letterId] = Letter(context, letterId, char.toLowerCase(), wordLetterId, isGuessed)
                    letterId++
                    isLetterNow = false
                }
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
                lettersString.append("_")
                lettersString.append(letter.letter)
                if (letter.wordLetterId != null) lettersString.append(letter.wordLetterId)
            }

            val editor = saves.edit()
            editor.putString(level, lettersString.toString())
            editor.apply()
        }

        letters = newLetters.requireNoNulls()
    }

    fun tipShowLetter(wordLetters: WordLetters): Letter {
        var letter: Letter
        do {
            letter = letters[(0 until letterCount).random()]
        } while (letter.wordLetterId == null || letter.isTipGuessed)
        wordLetters.tipLetterGuessed(letter)
        return letter
    }

    fun tipRemoveLetters(wordLetters: WordLetters) {
        for (letter in letters) {
            if (letter.wordLetterId == null) {
                if (letter.currentWordLetter != null) wordLetters.removeLetter(letter.currentWordLetter!!)
                letter.gone(wordLetters.animationDuration)
            }
        }
    }

    fun showChosenLetters(wordLetters: WordLetters) {
        for (letter in letters) {
            if (letter.isTipGuessed) wordLetters.tipLetterGuessed(letter)
        }
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

fun ClosedRange<Int>.random() = Random().nextInt((endInclusive + 1) - start) + start

fun String.remove(char: Char) = this.replace(char.toString(), "")

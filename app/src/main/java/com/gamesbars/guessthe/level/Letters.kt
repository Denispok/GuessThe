package com.gamesbars.guessthe.level

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.gamesbars.guessthe.R
import java.util.*

class Letters(private val activity: Activity, word: String, pack: String) {

    companion object {
        const val letterCount = 18
    }

    private var alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя012345"
    private val letters: Array<Letter>
    var removeTipUsed = false

    init {
        val saves = activity.getSharedPreferences("saves", Context.MODE_PRIVATE)
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
                    val wordLetterId = if (id + 1 < letterString.length && letterString[id + 1].isDigit()) {
                        if (id + 2 < letterString.length && letterString[id + 2].isDigit()) letterString.substring(id + 1..id + 2).toInt()
                        else letterString[id + 1].toString().toInt()
                    } else null

                    val isGuessed = if (id + 1 < letterString.length && letterString[id + 1] == '*') true
                        else if (id + 2 < letterString.length && letterString[id + 2] == '*') true
                        else if (id + 3 < letterString.length && letterString[id + 3] == '*') true
                        else false

                    newLetters[letterId] = Letter(activity, letterId, char.toLowerCase(), wordLetterId, isGuessed)
                    letterId++
                    isLetterNow = false
                }
            }
        } else {
            var spaceCount = 0
            for (id in 0 until word.length) {
                if (id + 1 - spaceCount > letterCount) break
                if (word[id] == ' ') {
                    spaceCount++
                    continue
                }
                var random: Int
                do {
                    random = (0 until letterCount).random()
                } while (newLetters[random] != null)
                newLetters[random] = Letter(activity, random, word[id], id)
                alphabet = alphabet.remove(word[id])
            }

            for (id in (0 until letterCount)) {
                if (newLetters[id] == null) {
                    val newLetter = alphabet[(0 until alphabet.length).random()]
                    alphabet = alphabet.remove(newLetter)
                    newLetters[id] = Letter(activity, id, newLetter)
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
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val layoutWidth = (letters[0].dimension + 2 * letters[0].margin) * (letterCount / 2)

        val params = LinearLayout.LayoutParams(letters[0].dimension, letters[0].dimension, 1f)
        val margin = activity.resources.getDimension(R.dimen.letter_margins_small).toInt()
        params.setMargins(margin, margin, margin, margin)

        letters.forEach { letter ->
            letter.parent?.also { (it as ViewGroup).removeView(letter) }
            if (layoutWidth > displayWidth) letter.layoutParams = params
        }

        for (i in 1..letterCount) {
            if (i <= letterCount / 2) linearLayout1.addView(letters[i - 1])
            else linearLayout2.addView(letters[i - 1])
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

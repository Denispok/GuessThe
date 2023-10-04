package com.gamesbars.guessthe.level

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.data.Storage
import com.gamesbars.guessthe.level.WordLetters.Companion.STATIC_CHARS
import java.util.Random

class Letters(private val activity: Activity, word: String, pack: String) {

    companion object {
        const val letterCount = 18
    }

    private var alphabet = activity.resources.getString(R.string.alphabet)
    private val letters: Array<Letter>
    var removeTipUsed = false

    init {
        val saves = activity.getSharedPreferences("saves", Context.MODE_PRIVATE)
        val level = Storage.getLevelName(pack, Storage.getCurrentLevel(pack))
        val newLetters = arrayOfNulls<Letter>(letterCount)
        val letterString = saves.getString(level, null)

        if (letterString != null && getLettersCount(letterString) == letterCount) {
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

                    newLetters[letterId] = Letter(activity, letterId, char.lowercaseChar(), wordLetterId, isGuessed)
                    letterId++
                    isLetterNow = false
                }
            }
        }

        if (newLetters[0] == null || !isLettersCorrect(word, newLetters.requireNoNulls())) {
            for (i in 0 until newLetters.size) newLetters[i] = null

            var staticCharsCount = 0
            for (id in 0 until word.length) {
                if (id + 1 - staticCharsCount > letterCount) break
                if (STATIC_CHARS.contains(word[id])) {
                    staticCharsCount++
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

    private fun isLettersCorrect(word: String, letters: Array<Letter>): Boolean {
        var wordShouldBeGuessed = word
        STATIC_CHARS.forEach {
            wordShouldBeGuessed = wordShouldBeGuessed.remove(it)
        }
        wordShouldBeGuessed = wordShouldBeGuessed.slice(0 until minOf(letterCount, wordShouldBeGuessed.length))
        for (i in letters) {
            wordShouldBeGuessed = wordShouldBeGuessed.replaceFirst(i.letter.toString(), "")
        }
        return wordShouldBeGuessed.isEmpty()
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

    private fun getLettersCount(letterString: String): Int {
        return letterString.split("_").size - 1
    }

    private fun ClosedRange<Int>.random() = Random().nextInt((endInclusive + 1) - start) + start

    private fun String.remove(char: Char) = this.replace(char.toString(), "")
}
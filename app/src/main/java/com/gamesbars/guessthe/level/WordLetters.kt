package com.gamesbars.guessthe.level

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Handler
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.customview.LettersLayout
import com.gamesbars.guessthe.fragment.LevelFragment
import com.gamesbars.guessthe.level.Letters.Companion.letterCount
import com.gamesbars.guessthe.playSound

class WordLetters(private val fragment: LevelFragment, private val word: String) {
    private lateinit var wordLayout: LettersLayout
    private val wordLetters = arrayListOf<WordLetter>()
    val animationDuration = 400L

    init {
        var spaceCount = 0
        for (i in 0 until word.length) {
            val knownChar = if (i + 1 - spaceCount > letterCount) word[i] else null
            val wordLetter = WordLetter(fragment.context!!, word[i] == ' ', knownChar)
            if (wordLetter.isSpace) spaceCount++
            wordLetters.add(wordLetter)
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
            if (wordLetter.text.isEmpty()) return // Be careful - model depends on view
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

    fun addLettersToLayout(lettersLayout: LettersLayout) {
        wordLayout = lettersLayout
        for (letter in wordLetters) {
            letter.parent?.also { (it as ViewGroup).removeView(letter) }
        }
        lettersLayout.setWordLetters(wordLetters)
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

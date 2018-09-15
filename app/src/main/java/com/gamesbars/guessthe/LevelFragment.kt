package com.gamesbars.guessthe

import android.app.AlertDialog
import android.content.Context
import android.database.SQLException
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class LevelFragment : Fragment() {

    private lateinit var image: String
    private lateinit var word: String
    private lateinit var pack: String
    private lateinit var letters: Letters
    private lateinit var wordLetters: WordLetters
    private var isFirstStart = true

    companion object {
        fun newInstance(pack: String): LevelFragment {
            val args = Bundle()
            args.putString("pack", pack)
            val fragment = LevelFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        }

        pack = arguments!!.getString("pack")
        loadLevel(pack)

        wordLetters = WordLetters(this, word)
        letters = Letters(context!!, word, pack)
        letters.setLettersOnClickListener {
            wordLetters.addLetter(it as Letter)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_level, container, false)
    }

    override fun onStart() {
        super.onStart()
        if (isFirstStart) {
            activity!!.findViewById<ImageView>(R.id.level_image).setImageResource(
                    resources.getIdentifier(image, "drawable", context!!.packageName))
            wordLetters.addLettersToLayout(activity!!.findViewById(R.id.level_word))
            letters.addLettersToLayout(activity!!.findViewById(R.id.level_letters_1),
                    activity!!.findViewById(R.id.level_letters_2))

            activity!!.findViewById<TextView>(R.id.level_tips).setOnClickListener { tips() }

            Handler().postDelayed({
                letters.showChosenLetters(wordLetters)
                if (letters.removeTipUsed) letters.tipRemoveLetters(wordLetters)
            }, 100L)

            isFirstStart = false
        }
    }

    private fun loadLevel(pack: String) {
        val saves = activity!!.getSharedPreferences("saves", Context.MODE_PRIVATE)

        if (!saves.contains(pack)) {
            val editor = saves.edit()
            editor.putInt(pack, 1)
            editor.apply()
        }

        val dBHelper = DataBaseHelper(context)

        try {
            dBHelper.openDataBase()
        } catch (sqle: SQLException) {
            throw sqle
        }

        val keyWord = "word"
        val keyImage = "image"

        val cursor = dBHelper.db.query(pack, arrayOf(keyWord, keyImage),
                "rowid = " + saves.getInt(pack, 1).toString(), null,
                null, null, null)

        cursor.moveToNext()

        image = cursor.getString(cursor.getColumnIndex(keyImage))
        word = cursor.getString(cursor.getColumnIndex(keyWord))

        cursor.close()
    }

    private fun tips() {
        val builder = AlertDialog.Builder(context)
        builder.setView(layoutInflater.inflate(R.layout.dialog_tips, null))
        // COINS CHECK
        val tips = builder.create()
        tips.show()

        val saves = context!!.getSharedPreferences("saves", Context.MODE_PRIVATE)
        val levelName = pack + saves.getInt(pack, 0)
        var levelString = saves.getString(levelName, "")

        tips.findViewById<RelativeLayout>(R.id.tips_letter).setOnClickListener {
            val letter = letters.tipShowLetter(wordLetters)

            val replacedString = letter.letter + letter.wordLetterId!!.toString()
            levelString = levelString.replace(replacedString, replacedString.toUpperCase())

            val editor = saves.edit()
            editor.putString(levelName, levelString)

            // COINS
            editor.apply()

            tips.cancel()
        }
        tips.findViewById<RelativeLayout>(R.id.tips_remove).setOnClickListener {
            letters.tipRemoveLetters(wordLetters)

            val editor = saves.edit()
            editor.putString(levelName, "$levelString!")

            // COINS
            editor.apply()

            tips.cancel()
        }
        tips.findViewById<RelativeLayout>(R.id.tips_skip).setOnClickListener {
            tips.cancel()
            win()
        }
    }

    fun win() {
        val saves = activity!!.getSharedPreferences("saves", Context.MODE_PRIVATE)
        val currentLevel = saves.getInt(pack, 1)
        val levelName = pack + currentLevel

        val editor = saves.edit()
        editor.putString(levelName, saves.getString(levelName, "").replace("!", "").toLowerCase())
        if (currentLevel > saves.getInt(pack + "completed", 0)) editor.putInt(pack + "completed", currentLevel)
        if (currentLevel + 1 > LevelMenuActivity.PACK_LEVELS_COUNT) editor.putInt(pack, 1)
        else editor.putInt(pack, currentLevel + 1)
        editor.apply()

        val fragment = WinFragment.newInstance(word, image, arguments!!.getString("pack"))
        fragmentManager!!.beginTransaction()
                .replace(R.id.activity_play, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addSharedElement(activity!!.findViewById<ImageView>(R.id.level_image), "ImageTransition")
                .commit()
    }
}

package com.gamesbars.guessthe

import android.content.Context
import android.database.SQLException
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class LevelFragment : Fragment() {

    private lateinit var image: String
    private lateinit var word: String
    private lateinit var letters: Letters
    private lateinit var wordLetters: WordLetters

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

        val pack = arguments!!.getString("pack")
        loadLevel(pack)

        wordLetters = WordLetters(context!!, word)
        letters = Letters(context!!, word, pack)
        letters.setLettersOnClickListener {
            wordLetters.addLetter(it as Letter)
        }
        letters.showChosenLetters(wordLetters)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_level, container, false)
    }

    override fun onStart() {
        super.onStart()
        activity!!.findViewById<ImageView>(R.id.level_image).setImageResource(
                resources.getIdentifier(image, "drawable", context!!.packageName))
        wordLetters.addLettersToLayout(activity!!.findViewById(R.id.level_word))
        letters.addLettersToLayout(activity!!.findViewById(R.id.level_letters_1),
                activity!!.findViewById(R.id.level_letters_2))
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
}

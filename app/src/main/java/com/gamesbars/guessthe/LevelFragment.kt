package com.gamesbars.guessthe

import android.content.Context
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
import android.widget.TextView

class LevelFragment : Fragment() {

    private lateinit var image: String
    private lateinit var word: String
    lateinit var pack: String
    lateinit var letters: Letters
    lateinit var wordLetters: WordLetters
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

        val currentLevel = saves.getInt(pack, 1)

        image = pack + currentLevel
        word = resources.getStringArray(resources.getIdentifier(pack, "array", context!!.packageName))[currentLevel - 1]
    }

    private fun tips() {
        val dialog = TipsDialogFragment()

        // COINS CHECK
        dialog.show(fragmentManager, resources.getString(R.string.tips_dialog_fragment_tag))
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
                .replace(R.id.activity_play, fragment, resources.getString(R.string.win_fragment_tag))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addSharedElement(activity!!.findViewById<ImageView>(R.id.level_image), "ImageTransition")
                .commit()
    }
}

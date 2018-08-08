package com.gamesbars.guessthe

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class LevelFragment : Fragment() {

    lateinit var letters: Letters
    lateinit var wordLetters: WordLetters

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

        //  Here's level load
        arguments!!.getString("pack")

        //  Here's letters inflating
        wordLetters = WordLetters (context!!, "очп мак")
        letters = Letters(context!!, "очп мак")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_level, container, false)
    }

    override fun onStart() {
        super.onStart()
        wordLetters.addLettersToLayout(activity!!.findViewById(R.id.level_word))
        letters.addLettersToLayout(activity!!.findViewById(R.id.level_letters_1),
                activity!!.findViewById(R.id.level_letters_2))
    }
}
package com.gamesbars.guessthe

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout

class LevelFragment : Fragment() {

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
        //val lettersView = activity!!.findViewById<GridLayout>(R.id.play_letters)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_level, container, false)
    }

    override fun onStart() {
        super.onStart()
        val letters = Letters(context!!, "очпочмак", activity!!.findViewById(R.id.level_letters_1),
                activity!!.findViewById(R.id.level_letters_2))
    }
}
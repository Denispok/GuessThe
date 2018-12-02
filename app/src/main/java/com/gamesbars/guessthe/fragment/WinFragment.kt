package com.gamesbars.guessthe.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.playSound

class WinFragment : Fragment() {

    private lateinit var image: String
    private lateinit var word: String
    private lateinit var pack: String
    private var isLevelReward: Boolean = false

    companion object {
        fun newInstance(word: String, image: String, pack: String, isLevelReward: Boolean): WinFragment {
            val args = Bundle()
            args.putString("word", word)
            args.putString("image", image)
            args.putString("pack", pack)
            args.putBoolean("isLevelReward", isLevelReward)
            val fragment = WinFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        }

        word = arguments!!.getString("word")
        image = arguments!!.getString("image")
        pack = arguments!!.getString("pack")
        isLevelReward = arguments!!.getBoolean("isLevelReward")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_win, container, false)
    }

    override fun onStart() {
        super.onStart()
        playSound(context!!, R.raw.win)
        activity!!.findViewById<ImageView>(R.id.win_image).setImageResource(
                resources.getIdentifier(image, "drawable", context!!.packageName))
        activity!!.findViewById<TextView>(R.id.win_word).text = word
        activity!!.findViewById<Button>(R.id.win_continue).setOnClickListener { nextLevel() }
        if (isLevelReward) {
            activity!!.findViewById<TextView>(R.id.win_reward_coins).text = activity!!.resources.getInteger(R.integer.level_reward).toString()
        } else {
            activity!!.findViewById<TextView>(R.id.win_reward_coins).visibility = View.GONE
            activity!!.findViewById<TextView>(R.id.win_x3).text = (2 * activity!!.resources.getInteger(R.integer.level_reward)).toString()
        }
    }

    private fun nextLevel() {
        if (activity!!.getSharedPreferences("saves", Context.MODE_PRIVATE).getInt(pack, 1) == 1) {
            activity!!.finish()
        } else {
            val fragment = LevelFragment.newInstance(pack)
            fragmentManager!!.beginTransaction()
                    .replace(R.id.activity_play_fragment, fragment, resources.getString(R.string.level_fragment_tag))
                    .addSharedElement(activity!!.findViewById<ImageView>(R.id.win_image), "ImageTransition")
                    .commit()
        }
    }
}

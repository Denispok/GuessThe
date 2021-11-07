package com.gamesbars.guessthe.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.PlayActivity
import com.gamesbars.guessthe.screen.PlayActivity.Companion.INTERSTITIAL_AD_FREQUENCY
import kotlinx.android.synthetic.main.fragment_win.*

class WinFragment : Fragment() {

    private lateinit var saves: SharedPreferences
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

        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)

        saves = context!!.getSharedPreferences("saves", Context.MODE_PRIVATE)
        word = arguments!!.getString("word")!!
        image = arguments!!.getString("image")!!
        pack = arguments!!.getString("pack")!!
        isLevelReward = arguments!!.getBoolean("isLevelReward")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_win, container, false)
    }

    override fun onStart() {
        super.onStart()
        playSound(context!!, R.raw.win)
        winImage.setImageResource(Storage.getWinImageResId(image))
        winWord.text = word

        if (isLevelReward) {
            winRewardCoins.text = activity!!.resources.getInteger(R.integer.level_reward).toString()
        } else {
            winRewardCoins.visibility = View.GONE
            winX3.text = (2 * activity!!.resources.getInteger(R.integer.level_reward)).toString()
        }

        winRewardedVideo.setOnClickListener {
            (activity!! as PlayActivity).showRewardedVideoAd()
        }

        winContinue.setOnClickListener {
            nextLevel()
        }
    }

    private fun nextLevel() {
        if (saves.getInt(pack, 1) == 1) {
            activity!!.finish()
        } else {
            val fragment = LevelFragment.newInstance(pack)
            fragmentManager!!.beginTransaction()
                .replace(R.id.activity_play_fragment, fragment, resources.getString(R.string.level_fragment_tag))
                .addSharedElement(winImage, "ImageTransition")
                .commit()
        }
        if (saves.getInt(pack, 1) % INTERSTITIAL_AD_FREQUENCY == 1)
            (activity!! as PlayActivity).showInterstitialAd()
    }
}

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
import com.gamesbars.guessthe.hasConnection
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.INTERSTITIAL_AD_FREQUENCY
import com.gamesbars.guessthe.screen.PlayActivity
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
            if (hasConnection(context!!)) {
                if (saves.getInt("without_connection", 0) != 0) {
                    saves.edit().apply {
                        putInt("without_connection", 0)
                        apply()
                    }
                    (activity!! as PlayActivity).loadBannerAd()
                }
                nextLevel()
            } else {
                saves.edit().apply {
                    putInt("without_connection", saves.getInt("without_connection", 0) + 1)
                    apply()
                }
                if (saves.getInt("without_connection", 0) >= 3)
                    InternetConnectionDialog().show(fragmentManager!!, getString(R.string.internet_connection_dialog_fragment_tag))
                else
                    nextLevel()
            }
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

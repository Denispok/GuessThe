package com.gamesbars.guessthe.fragment

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage
import com.gamesbars.guessthe.ads.AdsUtils
import com.gamesbars.guessthe.databinding.FragmentWinBinding
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.PlayActivity
import com.gamesbars.guessthe.screen.PlayActivity.Companion.INTERSTITIAL_AD_FREQUENCY

class WinFragment : Fragment() {

    private var _binding: FragmentWinBinding? = null
    private val binding get() = _binding!!
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

        word = arguments!!.getString("word")!!
        image = arguments!!.getString("image")!!
        pack = arguments!!.getString("pack")!!
        isLevelReward = arguments!!.getBoolean("isLevelReward")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        AdsUtils.fixDensity(resources)
        _binding = FragmentWinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        playSound(context!!, R.raw.win)
        binding.winImageIv.setImageResource(Storage.getWinImageResId(image))
        binding.winWordTv.text = word

        if (isLevelReward) {
            binding.rewardCoinsTv.text = activity!!.resources.getInteger(R.integer.level_reward).toString()
        } else {
            binding.rewardCoinsTv.visibility = View.GONE
            binding.rewardedX3Tv.text = (2 * activity!!.resources.getInteger(R.integer.level_reward)).toString()
        }

        binding.rewardedTextLl.setOnClickListener {
            (activity!! as PlayActivity).showRewardedVideoAd()
        }

        binding.nextBtn.setOnClickListener {
            nextLevel()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun nextLevel() {
        if (Storage.getCurrentLevel(pack) == 1) {
            activity!!.finish()
        } else {
            val fragment = LevelFragment.newInstance(pack)
            fragmentManager!!.beginTransaction()
                .replace(R.id.fragmentFl, fragment, resources.getString(R.string.level_fragment_tag))
                .addSharedElement(binding.winImageIv, "ImageTransition")
                .commit()
        }
        if (Storage.getCurrentLevel(pack) % INTERSTITIAL_AD_FREQUENCY == 1) {
            (activity!! as PlayActivity).showInterstitialAd()
        }
    }
}

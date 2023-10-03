package com.gamesbars.guessthe.screen

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage
import com.gamesbars.guessthe.ads.BannerAdDelegate
import com.gamesbars.guessthe.ads.InterstitialAdDelegate
import com.gamesbars.guessthe.ads.RewardedAdDelegate
import com.gamesbars.guessthe.databinding.ActivityPlayBinding
import com.gamesbars.guessthe.fragment.InfoDialogFragment
import com.gamesbars.guessthe.fragment.LevelFragment
import com.gamesbars.guessthe.fragment.TipsDialogFragment

class PlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayBinding
    private lateinit var saves: SharedPreferences
    private lateinit var rewardedAdDelegate: RewardedAdDelegate
    private lateinit var interstitialAdDelegate: InterstitialAdDelegate
    private lateinit var bannerAdDelegate: BannerAdDelegate
    var currentDialog: DialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        rewardedAdDelegate = RewardedAdDelegate(this, ::onRewardEarned)
        interstitialAdDelegate = InterstitialAdDelegate(this, saves)
        bannerAdDelegate = BannerAdDelegate(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentFl,
                    LevelFragment.newInstance(intent.extras!!.getString("pack")!!),
                    resources.getString(R.string.level_fragment_tag)
                )
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        updateBannerAd()
    }

    override fun onBackPressed() {
        if (currentDialog == null) super.onBackPressed()
        else if (currentDialog is TipsDialogFragment) (currentDialog as TipsDialogFragment).apply {
            updateFragment(supportFragmentManager.findFragmentByTag(resources.getString(R.string.level_fragment_tag)) as LevelFragment)
            dismiss()
        } else if (currentDialog is InfoDialogFragment) (currentDialog as InfoDialogFragment).apply {
            updateFragment(supportFragmentManager.findFragmentByTag(resources.getString(R.string.level_fragment_tag)) as LevelFragment)
            dismiss()
        }
    }

    fun showInterstitialAd(currentLevel: Int) {
        interstitialAdDelegate.showInterstitialAd(currentLevel)
    }

    fun showRewardedVideoAd() {
        rewardedAdDelegate.showRewardedVideoAd()
    }

    private fun updateBannerAd() {
        if (saves.getBoolean("ads", true)) {
            binding.adViewContainer.visibility = View.VISIBLE
            bannerAdDelegate.updateBanner(binding.adViewContainer)
        } else {
            binding.adViewContainer.visibility = View.GONE
        }
    }

    private fun onRewardEarned() {
        Storage.addCoins(2 * resources.getInteger(R.integer.level_reward))
        Toast.makeText(this, R.string.video_reward, Toast.LENGTH_LONG).show()

        findViewById<LinearLayout>(R.id.rewardedLl)?.isClickable = false
        findViewById<LinearLayout>(R.id.rewardedTextLl)?.visibility = View.GONE
        findViewById<TextView>(R.id.rewardedX3Tv)?.apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundColor(Color.TRANSPARENT)
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.coin_icon_16, 0)
            compoundDrawablePadding = resources.getDimension(R.dimen.win_coins_drawable_padding).toInt()
            text = "+".plus(2 * resources.getInteger(R.integer.level_reward))
        }
    }
}
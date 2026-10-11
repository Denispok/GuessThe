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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.BannerAdDelegate
import com.gamesbars.guessthe.ads.InterstitialAdDelegate
import com.gamesbars.guessthe.ads.RewardedAdDelegate
import com.gamesbars.guessthe.data.CoinsStorage
import com.gamesbars.guessthe.databinding.ActivityPlayBinding
import com.gamesbars.guessthe.fragment.InfoDialogFragment
import com.gamesbars.guessthe.fragment.LevelFragment
import com.gamesbars.guessthe.fragment.TipsDialogFragment
import com.gamesbars.guessthe.util.applySystemBarsCompat

class PlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayBinding
    private lateinit var saves: SharedPreferences
    private lateinit var rewardedAdDelegate: RewardedAdDelegate
    private lateinit var interstitialAdDelegate: InterstitialAdDelegate
    private lateinit var bannerAdDelegate: BannerAdDelegate

    private val dialogBackCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            when (val dialog = currentDialog) {
                is TipsDialogFragment -> {
                    dialog.updateFragment(findLevelFragment())
                    dialog.dismiss()
                }

                is InfoDialogFragment -> {
                    dialog.updateFragment(findLevelFragment())
                    dialog.dismiss()
                }
            }
        }
    }

    var currentDialog: DialogFragment? = null
        set(value) {
            field = value
            dialogBackCallback.isEnabled = value != null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsCompat()
        onBackPressedDispatcher.addCallback(this, dialogBackCallback)

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

    private fun findLevelFragment(): LevelFragment =
        supportFragmentManager.findFragmentByTag(resources.getString(R.string.level_fragment_tag)) as LevelFragment

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
        CoinsStorage.addCoins(2 * CoinsStorage.getLevelReward())
        Toast.makeText(this, R.string.video_reward, Toast.LENGTH_LONG).show()

        findViewById<LinearLayout>(R.id.rewardedLl)?.isClickable = false
        findViewById<LinearLayout>(R.id.rewardedTextLl)?.visibility = View.GONE
        findViewById<TextView>(R.id.rewardedX3Tv)?.apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundColor(Color.TRANSPARENT)
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.coin_icon_16, 0)
            compoundDrawablePadding = resources.getDimension(R.dimen.win_coins_drawable_padding).toInt()
            text = "+".plus(2 * CoinsStorage.getLevelReward())
        }
    }
}
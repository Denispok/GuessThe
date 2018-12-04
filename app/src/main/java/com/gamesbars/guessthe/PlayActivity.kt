package com.gamesbars.guessthe

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.gamesbars.guessthe.fragment.LevelFragment
import com.gamesbars.guessthe.fragment.TipsDialogFragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_play.*

class PlayActivity : AppCompatActivity(), RewardedVideoAdListener {

    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    var currentDialog: TipsDialogFragment? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Exo_2/Exo2-Medium.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())

        hideSystemUI()
        setContentView(R.layout.activity_play)

        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        if (saves.getBoolean("ads", true)) {
            if (hasConnection(this)) loadBannerAd()

            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = getString(R.string.interstitial_id)
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    mInterstitialAd.loadAd(AdRequest.Builder().build())
                }
            }
            mInterstitialAd.loadAd(AdRequest.Builder().build())
        }

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this

        supportFragmentManager.beginTransaction()
                .replace(R.id.activity_play_fragment,
                        LevelFragment.newInstance(intent.extras.getString("pack")), resources.getString(R.string.level_fragment_tag))
                .commit()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onBackPressed() {
        currentDialog?.apply {
            updateFragment(supportFragmentManager.findFragmentByTag(resources.getString(R.string.level_fragment_tag)) as LevelFragment)
            dismiss()
        } ?: super.onBackPressed()
    }

    fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        adView.visibility = View.VISIBLE
        adView.loadAd(adRequest)
    }

    fun showInterstitialAd() {
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        if (saves.getBoolean("ads", true) && mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        }
    }

    fun showRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getString(R.string.rewarded_video_id),
                AdRequest.Builder().build())
        Toast.makeText(this, getString(R.string.video_is_loading), Toast.LENGTH_LONG).show()
    }

    override fun onRewardedVideoAdClosed() {}

    override fun onRewardedVideoAdLeftApplication() {}

    override fun onRewardedVideoAdLoaded() {
        mRewardedVideoAd.show()
    }

    override fun onRewardedVideoAdOpened() {}

    override fun onRewardedVideoCompleted() {}

    override fun onRewarded(p0: RewardItem?) {
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        saves.edit().apply {
            putInt("coins", saves.getInt("coins", 0) + 2 * resources.getInteger(R.integer.level_reward))
            apply()
        }
        findViewById<LinearLayout>(R.id.win_rewarded_video).isClickable = false
        findViewById<LinearLayout>(R.id.win_x3_text).visibility = View.GONE
        findViewById<TextView>(R.id.win_x3).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundColor(Color.TRANSPARENT)
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.coin_icon_16, 0)
            compoundDrawablePadding = resources.getDimension(R.dimen.win_coins_drawable_padding).toInt()
            text = "+".plus(2 * resources.getInteger(R.integer.level_reward))
        }
    }

    override fun onRewardedVideoStarted() {}

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        Toast.makeText(this, getString(R.string.video_error), Toast.LENGTH_LONG).show()
    }
}

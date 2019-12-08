package com.gamesbars.guessthe

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.gamesbars.guessthe.fragment.InfoDialogFragment
import com.gamesbars.guessthe.fragment.LevelFragment
import com.gamesbars.guessthe.fragment.TipsDialogFragment
import com.google.android.gms.ads.AdListener
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

const val INTERSTITIAL_AD_FREQUENCY = 3 // every N level show ad

class PlayActivity : AppCompatActivity(), RewardedVideoAdListener {

    private lateinit var saves: SharedPreferences
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    var currentDialog: DialogFragment? = null

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

        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)

        if (saves.getBoolean("ads", true)) {
            if (hasConnection(this)) loadBannerAd()

            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = getString(R.string.interstitial_id)
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    mInterstitialAd.loadAd(buildAdRequest(saves))
                }
            }
            mInterstitialAd.loadAd(buildAdRequest(saves))
        }

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.activity_play_fragment,
                    LevelFragment.newInstance(intent.extras.getString("pack")), resources.getString(R.string.level_fragment_tag))
                .commit()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
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

    fun loadBannerAd() {
        if (saves.getBoolean("ads", true)) {
            adView.visibility = View.VISIBLE
            adView.loadAd(buildAdRequest(saves))
        }
    }

    fun showInterstitialAd() {
        if (saves.getBoolean("ads", true) && mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        }
    }

    fun showRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getString(R.string.rewarded_video_id), buildAdRequest(saves))
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
        saves.edit().apply {
            putInt("coins", saves.getInt("coins", 0) + 2 * resources.getInteger(R.integer.level_reward))
            apply()
        }
        Toast.makeText(this, R.string.video_reward, Toast.LENGTH_LONG).show()

        findViewById<LinearLayout>(R.id.win_rewarded_video)?.isClickable = false
        findViewById<LinearLayout>(R.id.win_x3_text)?.visibility = View.GONE
        findViewById<TextView>(R.id.win_x3)?.apply {
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

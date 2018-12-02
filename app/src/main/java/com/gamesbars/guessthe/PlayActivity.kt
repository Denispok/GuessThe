package com.gamesbars.guessthe

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.gamesbars.guessthe.fragment.LevelFragment
import com.gamesbars.guessthe.fragment.TipsDialogFragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_play.*

class PlayActivity : AppCompatActivity() {

    private lateinit var mInterstitialAd: InterstitialAd
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
            val adRequest = AdRequest.Builder().build()
            adView.visibility = View.VISIBLE
            adView.loadAd(adRequest)

            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = getString(R.string.interstitial_id)
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    mInterstitialAd.loadAd(AdRequest.Builder().build())
                }
            }
            mInterstitialAd.loadAd(AdRequest.Builder().build())
        }

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

    fun showInterstitialAd() {
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        if (saves.getBoolean("ads", true) && mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        }
    }
}

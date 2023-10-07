package com.gamesbars.guessthe.ads

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.appodeal.ads.Appodeal
import com.gamesbars.guessthe.util.RemoteConfig
import com.gamesbars.guessthe.util.TimeUtil

class InterstitialAdDelegate(
    private val activity: AppCompatActivity,
    private val saves: SharedPreferences
) {

    companion object {

        private const val INTERSTITIAL_AD_FREQUENCY = 3 // every N level show ad

        private var lastAdsShowedTimeMillis: Long? = null

        fun interstitialShown() {
            lastAdsShowedTimeMillis = TimeUtil.currentTimeMillis()
        }
    }

    fun showInterstitialAd(currentLevel: Int) {
        if (needToShow(currentLevel)) {
            showInterstitialAdAppodeal()
        }
    }

    private fun needToShow(currentLevel: Int): Boolean {
        if (!saves.getBoolean("ads", true)) return false

        return if (RemoteConfig.getNewInterstitialTimingEnabled()) {
            needToShowByTiming()
        } else {
            currentLevel % INTERSTITIAL_AD_FREQUENCY == 1
        }
    }

    private fun needToShowByTiming(): Boolean {
        val currentTime = TimeUtil.currentTimeMillis()

        return if (lastAdsShowedTimeMillis == null) {
            currentTime - TimeUtil.appLaunchTime >= RemoteConfig.getInterstitialStartDelay()
        } else {
            lastAdsShowedTimeMillis?.let { lastAdsShowedTimeMillis ->
                currentTime - lastAdsShowedTimeMillis >= RemoteConfig.getInterstitialBetweenDelay()
            } ?: false
        }
    }

    private fun showInterstitialAdAppodeal() {
        Appodeal.show(activity, Appodeal.INTERSTITIAL)
    }
}
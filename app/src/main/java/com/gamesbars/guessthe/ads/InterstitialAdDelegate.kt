package com.gamesbars.guessthe.ads

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.appodeal.ads.Appodeal

class InterstitialAdDelegate(
    private val activity: AppCompatActivity,
    private val saves: SharedPreferences
) {

    fun showInterstitialAd() {
        if (saves.getBoolean("ads", true)) {
            showInterstitialAdAppodeal()
        }
    }

    private fun showInterstitialAdAppodeal() {
        Appodeal.show(activity, Appodeal.INTERSTITIAL)
    }
}
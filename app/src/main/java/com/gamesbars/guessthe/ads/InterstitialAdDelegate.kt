package com.gamesbars.guessthe.ads

import android.content.SharedPreferences
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.gamesbars.guessthe.AnalyticsHelper
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.buildAdRequest
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdDelegate(
    private val activity: AppCompatActivity,
    private val saves: SharedPreferences
) {

    companion object {
        private const val AD_RETRY_TIMEOUT = 15000L
    }

    private var mInterstitialAd: InterstitialAd? = null

    fun loadInterstitialAd() {
        InterstitialAd.load(
            activity,
            activity.getString(R.string.interstitial_id),
            buildAdRequest(saves),
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAd.fullScreenContentCallback = InterstitialAdFullScreenContentCallback()
                    mInterstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    AnalyticsHelper.logAdsError("InterstitialAdFailedToLoad: ${adError.message}")
                    mInterstitialAd = null
                    Handler(activity.mainLooper).postDelayed({
                        if (activity.lifecycle.currentState != Lifecycle.State.DESTROYED) loadInterstitialAd()
                    }, AD_RETRY_TIMEOUT)
                }
            }
        )
    }

    fun showInterstitialAd() {
        if (saves.getBoolean("ads", true)) {
            mInterstitialAd?.show(activity) ?: AnalyticsHelper.logAdsError("InterstitialAd is null")
        }
    }

    inner class InterstitialAdFullScreenContentCallback : FullScreenContentCallback() {

        override fun onAdDismissedFullScreenContent() {
            mInterstitialAd = null
            loadInterstitialAd()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            AnalyticsHelper.logAdsError("InterstitialAdFailedToShowFullScreenContent: ${adError.message}")
            mInterstitialAd = null
            loadInterstitialAd()
        }
    }
}
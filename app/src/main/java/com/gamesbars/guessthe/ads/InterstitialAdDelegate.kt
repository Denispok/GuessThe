package com.gamesbars.guessthe.ads

import android.content.SharedPreferences
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.appodeal.ads.Appodeal
import com.gamesbars.guessthe.AnalyticsHelper
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.AdsUtils.buildAdmobAdRequest
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
        when (AdsUtils.AD_MEDIATION_TYPE) {
            AdsUtils.AD_MEDIATION_TYPE_ADMOB -> loadInterstitialAdAdmob()
            AdsUtils.AD_MEDIATION_TYPE_APPODEAL -> Unit // Appodeal doesn't need preload
        }
    }

    fun showInterstitialAd() {
        if (saves.getBoolean("ads", true)) {
            when (AdsUtils.AD_MEDIATION_TYPE) {
                AdsUtils.AD_MEDIATION_TYPE_ADMOB -> showInterstitialAdAdmob()
                AdsUtils.AD_MEDIATION_TYPE_APPODEAL -> showInterstitialAdAppodeal()
            }
        }
    }

    private fun loadInterstitialAdAdmob() {
        InterstitialAd.load(
            activity,
            activity.getString(R.string.interstitial_id),
            buildAdmobAdRequest(saves),
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAd.fullScreenContentCallback = InterstitialAdFullScreenContentCallback()
                    mInterstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    AnalyticsHelper.logInterstitialAdError("onAdFailedToLoad: ${adError.message}")
                    mInterstitialAd = null
                    Handler(activity.mainLooper).postDelayed({
                        if (activity.lifecycle.currentState != Lifecycle.State.DESTROYED) loadInterstitialAd()
                    }, AD_RETRY_TIMEOUT)
                }
            }
        )
    }

    private fun showInterstitialAdAdmob() {
        mInterstitialAd?.show(activity) ?: AnalyticsHelper.logInterstitialAdError("InterstitialAd is null")
    }

    private fun showInterstitialAdAppodeal() {
        Appodeal.show(activity, Appodeal.INTERSTITIAL)
    }

    inner class InterstitialAdFullScreenContentCallback : FullScreenContentCallback() {

        override fun onAdDismissedFullScreenContent() {
            mInterstitialAd = null
            loadInterstitialAd()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            AnalyticsHelper.logInterstitialAdError("onAdFailedToShowFullScreenContent: ${adError.message}")
            mInterstitialAd = null
            loadInterstitialAd()
        }
    }
}
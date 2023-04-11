package com.gamesbars.guessthe.ads

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appodeal.ads.Appodeal
import com.appodeal.ads.InterstitialCallbacks
import com.appodeal.ads.RewardedVideoCallbacks
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.gamesbars.guessthe.AnalyticsHelper.logAdSdkError
import com.gamesbars.guessthe.AnalyticsHelper.logInterstitialAdError
import com.gamesbars.guessthe.AnalyticsHelper.logRewardedAdError
import com.gamesbars.guessthe.R
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

object AdsUtils {

    const val AD_MEDIATION_TYPE_ADMOB = 0
    const val AD_MEDIATION_TYPE_APPODEAL = 1
    const val AD_MEDIATION_TYPE = AD_MEDIATION_TYPE_APPODEAL

    fun initMobileAds(activity: AppCompatActivity) {
        when (AD_MEDIATION_TYPE) {
            AD_MEDIATION_TYPE_APPODEAL -> initAppodeal(activity)
            AD_MEDIATION_TYPE_ADMOB -> initAdmob(activity)
        }
    }

    fun buildAdmobAdRequest(saves: SharedPreferences): AdRequest {
        val adBuilder = AdRequest.Builder()
        if (saves.getBoolean("npa", true)) {
            val extras = Bundle()
            extras.putString("npa", "1")
            adBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }
        return adBuilder.build()
    }

    private fun initAdmob(context: Context) {
        MobileAds.initialize(context)
    }

    private fun initAppodeal(activity: AppCompatActivity) {
        Appodeal.initialize(
            activity,
            activity.getString(R.string.appodeal_app_id),
            Appodeal.BANNER or Appodeal.INTERSTITIAL or Appodeal.REWARDED_VIDEO,
            object : ApdInitializationCallback {

                override fun onInitializationFinished(errors: List<ApdInitializationError>?) {
                    errors?.forEach { logAdSdkError("onInitializationFinished: ${it.message}") }
                }
            }
        )

        Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {

            override fun onInterstitialClicked() {
            }

            override fun onInterstitialClosed() {
            }

            override fun onInterstitialExpired() {
            }

            override fun onInterstitialFailedToLoad() {
                val isInitialized = Appodeal.isInitialized(Appodeal.INTERSTITIAL)
                val isLoaded = Appodeal.isLoaded(Appodeal.INTERSTITIAL)
                logInterstitialAdError("onInterstitialFailedToLoad: isInitialized=$isInitialized, isLoaded=$isLoaded")
            }

            override fun onInterstitialLoaded(isPrecache: Boolean) {
            }

            override fun onInterstitialShowFailed() {
                val isInitialized = Appodeal.isInitialized(Appodeal.INTERSTITIAL)
                val isLoaded = Appodeal.isLoaded(Appodeal.INTERSTITIAL)
                logInterstitialAdError("onInterstitialShowFailed: isInitialized=$isInitialized, isLoaded=$isLoaded")
            }

            override fun onInterstitialShown() {
            }
        })

        Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {

            override fun onRewardedVideoClicked() {
            }

            override fun onRewardedVideoClosed(finished: Boolean) {
            }

            override fun onRewardedVideoExpired() {
            }

            override fun onRewardedVideoFailedToLoad() {
                val isInitialized = Appodeal.isInitialized(Appodeal.REWARDED_VIDEO)
                val isLoaded = Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)
                logRewardedAdError("onRewardedVideoFailedToLoad: isInitialized=$isInitialized, isLoaded=$isLoaded")
            }

            override fun onRewardedVideoFinished(amount: Double, name: String?) {
            }

            override fun onRewardedVideoLoaded(isPrecache: Boolean) {
            }

            override fun onRewardedVideoShowFailed() {
                val isInitialized = Appodeal.isInitialized(Appodeal.REWARDED_VIDEO)
                val isLoaded = Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)
                logRewardedAdError("onRewardedVideoShowFailed: isInitialized=$isInitialized, isLoaded=$isLoaded")
            }

            override fun onRewardedVideoShown() {
            }
        })
    }
}
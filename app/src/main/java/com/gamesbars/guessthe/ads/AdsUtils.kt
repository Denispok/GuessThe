package com.gamesbars.guessthe.ads

import androidx.appcompat.app.AppCompatActivity
import com.appodeal.ads.Appodeal
import com.appodeal.ads.InterstitialCallbacks
import com.appodeal.ads.RewardedVideoCallbacks
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.AdsAnalytics.logAdSdkError
import com.gamesbars.guessthe.ads.AdsAnalytics.logInterstitialAdError
import com.gamesbars.guessthe.ads.AdsAnalytics.logRewardedAdError
import com.gamesbars.guessthe.ads.appodeal.AppodealAdRevenueCallbacks
import com.gamesbars.guessthe.util.RemoteConfig

object AdsUtils {

    fun initMobileAds(activity: AppCompatActivity) {
        initAppodeal(activity)
    }

    private fun initAppodeal(activity: AppCompatActivity) {
        Appodeal.setSmartBanners(RemoteConfig.getSmartBannersEnabled())
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
                InterstitialAdDelegate.interstitialShown()
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

            override fun onRewardedVideoFinished(amount: Double, currency: String) {
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

        Appodeal.setAdRevenueCallbacks(AppodealAdRevenueCallbacks())
    }
}
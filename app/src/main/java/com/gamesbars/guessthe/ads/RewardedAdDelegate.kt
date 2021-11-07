package com.gamesbars.guessthe.ads

import android.content.SharedPreferences
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.gamesbars.guessthe.AnalyticsHelper
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.buildAdRequest
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdDelegate(
    private val activity: AppCompatActivity,
    private val saves: SharedPreferences,
    private val onRewardEarned: () -> Unit
) {

    companion object {
        private const val AD_RETRY_TIMEOUT = 15000L
    }

    private var mRewardedAd: RewardedAd? = null

    fun loadRewardedAd() {
        RewardedAd.load(
            activity,
            activity.getString(R.string.rewarded_video_id),
            buildAdRequest(saves),
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    rewardedAd.fullScreenContentCallback = RewardedAdFullScreenContentCallback()
                    mRewardedAd = rewardedAd
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    AnalyticsHelper.logAdsError("RewardedAdFailedToLoad: ${adError.message}")
                    mRewardedAd = null
                    Handler(activity.mainLooper).postDelayed({
                        if (activity.lifecycle.currentState != Lifecycle.State.DESTROYED) loadRewardedAd()
                    }, AD_RETRY_TIMEOUT)
                }
            }
        )
    }

    fun showRewardedVideoAd() {
        if (mRewardedAd != null) {
            mRewardedAd?.show(activity) { _ ->
                onRewardEarned.invoke()
            }
        } else {
            AnalyticsHelper.logAdsError("RewardedVideoAd is null")
            Toast.makeText(activity, activity.getString(R.string.video_error), Toast.LENGTH_LONG).show()
        }
    }

    inner class RewardedAdFullScreenContentCallback : FullScreenContentCallback() {

        override fun onAdDismissedFullScreenContent() {
            mRewardedAd = null
            loadRewardedAd()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            AnalyticsHelper.logAdsError("RewardedAdFailedToShowFullScreenContent: ${adError.message}")
            mRewardedAd = null
            loadRewardedAd()
        }
    }
}
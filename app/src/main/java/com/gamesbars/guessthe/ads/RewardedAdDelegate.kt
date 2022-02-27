package com.gamesbars.guessthe.ads

import android.content.SharedPreferences
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
import kotlinx.coroutines.*

class RewardedAdDelegate(
    private val activity: AppCompatActivity,
    private val saves: SharedPreferences,
    private val onRewardEarned: () -> Unit
) {

    companion object {
        private const val AD_RETRY_TIMEOUT = 15000L
    }

    private var mRewardedAd: RewardedAd? = null
    private var adState: AdState = AdState.NONE
    private var adRetryJob: Job? = null
    private var rewardedAdLoadCallback: RewardedAdLoadCallback? = null

    fun loadRewardedAd() {
        adState = AdState.LOADING
        RewardedAd.load(
            activity,
            activity.getString(R.string.rewarded_video_id),
            buildAdRequest(saves),
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    adState = AdState.READY
                    rewardedAd.fullScreenContentCallback = RewardedAdFullScreenContentCallback()
                    mRewardedAd = rewardedAd
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adState = AdState.ERROR_DELAY
                    AnalyticsHelper.logRewardedAdError("onAdFailedToLoad: ${adError.message}")
                    mRewardedAd = null

                    adRetryJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(AD_RETRY_TIMEOUT)
                        if (activity.lifecycle.currentState != Lifecycle.State.DESTROYED) loadRewardedAd()
                    }
                }
            }.also { rewardedAdLoadCallback = it }
        )
    }

    fun showRewardedVideoAd() {
        if (mRewardedAd != null) {
            mRewardedAd?.show(activity) { _ ->
                onRewardEarned.invoke()
            }
        } else {
            AnalyticsHelper.logRewardedAdError("RewardedAd is null, state: $adState")

            if (adState == AdState.LOADING) {
                Toast.makeText(activity, activity.getString(R.string.video_is_loading), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, activity.getString(R.string.video_error), Toast.LENGTH_LONG).show()
            }

            if (adState == AdState.ERROR_DELAY) {
                adRetryJob?.cancel()
                loadRewardedAd()
            }
        }
    }

    inner class RewardedAdFullScreenContentCallback : FullScreenContentCallback() {

        override fun onAdDismissedFullScreenContent() {
            mRewardedAd = null
            loadRewardedAd()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            AnalyticsHelper.logRewardedAdError("onAdFailedToShowFullScreenContent: ${adError.message}")
            mRewardedAd = null
            loadRewardedAd()
        }
    }

    enum class AdState {
        NONE,
        LOADING,
        ERROR_DELAY,
        READY
    }
}
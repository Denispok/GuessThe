package com.gamesbars.guessthe.ads

import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.appodeal.ads.Appodeal
import com.appodeal.ads.RewardedVideoCallbacks
import com.gamesbars.guessthe.AnalyticsHelper.logRewardedAdError
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.AdsUtils.buildAdmobAdRequest
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
        when (AdsUtils.AD_MEDIATION_TYPE) {
            AdsUtils.AD_MEDIATION_TYPE_ADMOB -> loadRewardedAdAdmob()
            AdsUtils.AD_MEDIATION_TYPE_APPODEAL -> Unit // Appodeal doesn't need preload
        }
    }

    fun showRewardedVideoAd() {
        when (AdsUtils.AD_MEDIATION_TYPE) {
            AdsUtils.AD_MEDIATION_TYPE_ADMOB -> showRewardedVideoAdAdmob()
            AdsUtils.AD_MEDIATION_TYPE_APPODEAL -> showRewardedVideoAdAppodeal()
        }
    }

    private fun loadRewardedAdAdmob() {
        adState = AdState.LOADING
        RewardedAd.load(
            activity,
            activity.getString(R.string.rewarded_video_id),
            buildAdmobAdRequest(saves),
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    adState = AdState.READY
                    rewardedAd.fullScreenContentCallback = RewardedAdFullScreenContentCallback()
                    mRewardedAd = rewardedAd
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adState = AdState.ERROR_DELAY
                    logRewardedAdError("onAdFailedToLoad: ${adError.message}")
                    mRewardedAd = null

                    adRetryJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(AD_RETRY_TIMEOUT)
                        if (activity.lifecycle.currentState != Lifecycle.State.DESTROYED) loadRewardedAd()
                    }
                }
            }.also { rewardedAdLoadCallback = it }
        )
    }

    private fun showRewardedVideoAdAdmob() {
        if (mRewardedAd != null) {
            mRewardedAd?.show(activity) { _ ->
                onRewardEarned.invoke()
            }
        } else {
            logRewardedAdError("RewardedAd is null, state: $adState")

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

    private fun showRewardedVideoAdAppodeal() {
        if (!Appodeal.isInitialized(Appodeal.REWARDED_VIDEO)) {
            Toast.makeText(activity, activity.getString(R.string.video_error), Toast.LENGTH_LONG).show()
        } else if (!Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {
            Toast.makeText(activity, activity.getString(R.string.video_is_loading), Toast.LENGTH_SHORT).show()
        } else {
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

                override fun onRewardedVideoFinished(amount: Double, currency: String?) {
                    onRewardEarned.invoke()
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
            Appodeal.show(activity, Appodeal.REWARDED_VIDEO)
        }
    }

    inner class RewardedAdFullScreenContentCallback : FullScreenContentCallback() {

        override fun onAdDismissedFullScreenContent() {
            mRewardedAd = null
            loadRewardedAd()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            logRewardedAdError("onAdFailedToShowFullScreenContent: ${adError.message}")
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
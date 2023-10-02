package com.gamesbars.guessthe.ads

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appodeal.ads.Appodeal
import com.appodeal.ads.RewardedVideoCallbacks
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.AdsAnalytics.logRewardedAdError

class RewardedAdDelegate(
    private val activity: AppCompatActivity,
    private val onRewardEarned: () -> Unit
) {

    fun showRewardedVideoAd() {
        showRewardedVideoAdAppodeal()
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
}
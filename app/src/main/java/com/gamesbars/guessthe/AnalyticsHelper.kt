package com.gamesbars.guessthe

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsHelper {

    private const val TAG_DEBUG = "DEBUG"
    private const val EVENT_ERROR_AD_REWARDED = "error_ad_rewarded"
    private const val EVENT_ERROR_AD_INTERSTITIAL = "error_ad_interstitial"
    private const val EVENT_LEVEL_COMPLETE = "level_complete"
    private const val PARAM_MESSAGE = "message"
    private const val PARAM_CONNECTIVITY_INFO = "connectivity_info"

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(App.appContext)

    fun logInterstitialAdError(message: String) {
        val params = Bundle()
        val connectivityInfo = getConnectivityInfo()

        if (connectivityInfo != null) params.putString(PARAM_CONNECTIVITY_INFO, connectivityInfo.sliceUntilIndex(99))
        params.putString(PARAM_MESSAGE, message.sliceUntilIndex(99))

        if (BuildConfig.DEBUG) {
            Log.d(TAG_DEBUG, "FA event $EVENT_ERROR_AD_INTERSTITIAL: $params")
        } else {
            firebaseAnalytics.logEvent(EVENT_ERROR_AD_INTERSTITIAL, params)
        }
    }

    fun logRewardedAdError(message: String) {
        val params = Bundle()
        val connectivityInfo = getConnectivityInfo()

        if (connectivityInfo != null) params.putString(PARAM_CONNECTIVITY_INFO, connectivityInfo.sliceUntilIndex(99))
        params.putString(PARAM_MESSAGE, message.sliceUntilIndex(99))

        if (BuildConfig.DEBUG) {
            Log.d(TAG_DEBUG, "FA event $EVENT_ERROR_AD_REWARDED: $params")
        } else {
            firebaseAnalytics.logEvent(EVENT_ERROR_AD_REWARDED, params)
        }
    }

    fun logLevelComplete(pack: String, level: Int) {
        val params = Bundle()
        params.putString("completed_level", (pack + level).sliceUntilIndex(99))
        if (BuildConfig.DEBUG) {
            Log.d(TAG_DEBUG, "FA event $EVENT_LEVEL_COMPLETE: $params")
            Log.d(TAG_DEBUG, "FA set user property $pack to $level")
        } else {
            firebaseAnalytics.logEvent(EVENT_LEVEL_COMPLETE, params)
            firebaseAnalytics.setUserProperty(pack, level.toString())
        }
    }

    private fun getConnectivityInfo(): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cm = ContextCompat.getSystemService(App.appContext, ConnectivityManager::class.java)
            val hasInternet = cm?.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val hasValidated = cm?.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            return if (cm == null) {
                "ConnectivityManager is null"
            } else if (cm.activeNetwork == null) {
                "activeNetwork is null"
            } else {
                "hasInternet: $hasInternet, hasValidated: $hasValidated"
            }
        }
        return null
    }
}
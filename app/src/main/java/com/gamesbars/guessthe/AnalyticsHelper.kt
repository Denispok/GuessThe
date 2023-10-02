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
    private const val TAG_CONSENT = "CONSENT"
    private const val EVENT_ERROR_AD_SDK_APPODEAL = "error_ad_sdk_appodeal"
    private const val EVENT_ERROR_AD_REWARDED_APPODEAL = "error_ad_rewarded_appodeal"
    private const val EVENT_ERROR_AD_INTERSTITIAL_APPODEAL = "error_ad_interstitial_appodeal"
    private const val EVENT_ERROR_CONSENT_APPODEAL = "consent_error_appodeal"
    private const val EVENT_LEVEL_COMPLETE = "level_complete"
    private const val EVENT_PACK_UNLOCKED = "pack_unlocked"
    private const val PROPERTY_ADSLOCATION = "adslocation"
    private const val PARAM_MESSAGE = "message"
    private const val PARAM_CONNECTIVITY_INFO = "connectivity_info"

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(App.appContext)

    fun logAdSdkError(message: String) {
        val params = Bundle()
        val connectivityInfo = getConnectivityInfo()

        if (connectivityInfo != null) params.putString(PARAM_CONNECTIVITY_INFO, connectivityInfo.sliceUntilIndex(99))
        params.putString(PARAM_MESSAGE, message.sliceUntilIndex(99))

        if (BuildConfig.DEBUG) {
            Log.d(TAG_DEBUG, "FA event $EVENT_ERROR_AD_SDK_APPODEAL: $params")
        } else {
            firebaseAnalytics.logEvent(EVENT_ERROR_AD_SDK_APPODEAL, params)
        }
    }

    fun logInterstitialAdError(message: String) {
        val params = Bundle()
        val connectivityInfo = getConnectivityInfo()

        if (connectivityInfo != null) params.putString(PARAM_CONNECTIVITY_INFO, connectivityInfo.sliceUntilIndex(99))
        params.putString(PARAM_MESSAGE, message.sliceUntilIndex(99))

        val eventName = EVENT_ERROR_AD_INTERSTITIAL_APPODEAL

        if (BuildConfig.DEBUG) {
            Log.d(TAG_DEBUG, "FA event $eventName: $params")
        } else {
            firebaseAnalytics.logEvent(eventName, params)
        }
    }

    fun logRewardedAdError(message: String) {
        val params = Bundle()
        val connectivityInfo = getConnectivityInfo()

        if (connectivityInfo != null) params.putString(PARAM_CONNECTIVITY_INFO, connectivityInfo.sliceUntilIndex(99))
        params.putString(PARAM_MESSAGE, message.sliceUntilIndex(99))

        val eventName = EVENT_ERROR_AD_REWARDED_APPODEAL

        if (BuildConfig.DEBUG) {
            Log.d(TAG_DEBUG, "FA event $eventName: $params")
        } else {
            firebaseAnalytics.logEvent(eventName, params)
        }
    }

    fun logLevelComplete(pack: String, level: Int) {
        val params = Bundle()
        params.putString("completed_level", (Storage.getLevelName(pack, level)).sliceUntilIndex(99))
        if (BuildConfig.DEBUG) {
            Log.d(TAG_DEBUG, "FA event $EVENT_LEVEL_COMPLETE: $params")
            Log.d(TAG_DEBUG, "FA set user property $pack to $level")
        } else {
            firebaseAnalytics.logEvent(EVENT_LEVEL_COMPLETE, params)
            firebaseAnalytics.setUserProperty(pack, level.toString())
        }
    }

    fun logPackUnlock(pack: String, unlockType: String) {
        val params = Bundle()
        params.putString("pack", pack)
        params.putString("unlock_type", unlockType)
        if (BuildConfig.DEBUG) {
            Log.d(TAG_DEBUG, "FA event $EVENT_PACK_UNLOCKED: $params")
            Log.d(TAG_DEBUG, "FA set user property $pack to 0")
        } else {
            firebaseAnalytics.logEvent(EVENT_PACK_UNLOCKED, params)
            firebaseAnalytics.setUserProperty(pack, "0")
        }
    }

    fun logConsentError(errorMessage: String) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG_CONSENT, errorMessage)
        } else {
            val params = Bundle()
            params.putString("message", errorMessage.sliceUntilIndex(99))
            firebaseAnalytics.logEvent(EVENT_ERROR_CONSENT_APPODEAL, params)
        }
    }

    fun logAdsLocation(inEeaOrUnknown: Boolean) {
        val location = if (inEeaOrUnknown) "InEeaOrUnknown" else "None"
        if (BuildConfig.DEBUG) Log.d(TAG_DEBUG, "FA set user property $PROPERTY_ADSLOCATION to $location")
        else firebaseAnalytics.setUserProperty(PROPERTY_ADSLOCATION, location)
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
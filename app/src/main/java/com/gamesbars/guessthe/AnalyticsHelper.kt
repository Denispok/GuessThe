package com.gamesbars.guessthe

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsHelper {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(App.appContext)

    fun logAdsError(message: String) {
        val params = Bundle()
        params.putString("message", message.sliceUntilIndex(99))
        firebaseAnalytics.logEvent("ads_error", params)
    }

    fun logLevelComplete(pack: String, level: Int) {
        val params = Bundle()
        params.putString("completed_level", (pack + level).sliceUntilIndex(99))
        firebaseAnalytics.logEvent("level_complete", params)
        firebaseAnalytics.setUserProperty(pack, level.toString())
    }
}
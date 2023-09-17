package com.gamesbars.guessthe.ads

import com.appodeal.ads.revenue.RevenueInfo
import com.gamesbars.guessthe.App
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

object AdsAnalytics {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(App.appContext)

    fun logRevenueAppodeal(revenueInfo: RevenueInfo) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
            param(FirebaseAnalytics.Param.AD_PLATFORM, revenueInfo.platform)
            param(FirebaseAnalytics.Param.SOURCE, revenueInfo.networkName)
            param(FirebaseAnalytics.Param.AD_FORMAT, revenueInfo.adTypeString)
            param(FirebaseAnalytics.Param.AD_UNIT_NAME, revenueInfo.adUnitName)
            param(FirebaseAnalytics.Param.CURRENCY, revenueInfo.currency)
            param(FirebaseAnalytics.Param.VALUE, revenueInfo.revenue)
        }
    }
}
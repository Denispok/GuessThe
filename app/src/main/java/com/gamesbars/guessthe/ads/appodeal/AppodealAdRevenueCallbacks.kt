package com.gamesbars.guessthe.ads.appodeal

import com.appodeal.ads.revenue.AdRevenueCallbacks
import com.appodeal.ads.revenue.RevenueInfo
import com.gamesbars.guessthe.ads.AdsAnalytics

class AppodealAdRevenueCallbacks : AdRevenueCallbacks {

    override fun onAdRevenueReceive(revenueInfo: RevenueInfo) {
        AdsAnalytics.logRevenueAppodeal(revenueInfo)
    }
}
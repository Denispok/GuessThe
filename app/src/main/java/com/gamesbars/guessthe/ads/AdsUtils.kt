package com.gamesbars.guessthe.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds

object AdsUtils {

    fun initMobileAds(context: Context) {
        MobileAds.initialize(context)
    }
}
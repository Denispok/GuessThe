package com.gamesbars.guessthe.ads

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.appodeal.ads.Appodeal

class BannerAdDelegate(
    private val activity: AppCompatActivity,
) {

    /** Call in onResume */
    fun updateBanner(adViewContainer: ViewGroup) {
        loadBannerAppodeal(adViewContainer)
    }

    private fun loadBannerAppodeal(adViewContainer: ViewGroup) {
        val view = Appodeal.getBannerView(activity)
        adViewContainer.addView(view)
        Appodeal.show(activity, Appodeal.BANNER_VIEW)
    }
}
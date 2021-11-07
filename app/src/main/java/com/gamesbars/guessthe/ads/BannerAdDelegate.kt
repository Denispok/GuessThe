package com.gamesbars.guessthe.ads

import android.content.SharedPreferences
import android.util.DisplayMetrics
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.buildAdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

class BannerAdDelegate(
    private val activity: AppCompatActivity,
    private val saves: SharedPreferences
) {

    private lateinit var adView: AdView

    fun loadBanner(adViewContainer: ViewGroup) {
        adView = AdView(activity)
        adViewContainer.addView(adView)
        adView.adUnitId = activity.getString(R.string.banner_id)
        adView.adSize = getAdSize(adViewContainer)
        adView.loadAd(buildAdRequest(saves))
    }

    private fun getAdSize(adViewContainer: ViewGroup): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = adViewContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }
}
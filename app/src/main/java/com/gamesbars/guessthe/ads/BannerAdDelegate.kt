package com.gamesbars.guessthe.ads

import android.content.SharedPreferences
import android.util.DisplayMetrics
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.appodeal.ads.Appodeal
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.AdsUtils.buildAdmobAdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

class BannerAdDelegate(
    private val activity: AppCompatActivity,
    private val saves: SharedPreferences
) {

    private lateinit var adView: AdView

    /** Call on view creation */
    fun loadBanner(activity: AppCompatActivity, adViewContainer: ViewGroup) {
        when (AdsUtils.AD_MEDIATION_TYPE) {
            AdsUtils.AD_MEDIATION_TYPE_ADMOB -> loadBannerAdmob(adViewContainer)
            AdsUtils.AD_MEDIATION_TYPE_APPODEAL -> Unit // Appodeal recreating view every time during update
        }
    }

    /** Call in onResume */
    fun updateBanner(activity: AppCompatActivity, adViewContainer: ViewGroup) {
        when (AdsUtils.AD_MEDIATION_TYPE) {
            AdsUtils.AD_MEDIATION_TYPE_ADMOB -> Unit // Admob doesn't need update
            AdsUtils.AD_MEDIATION_TYPE_APPODEAL -> loadBannerAppodeal(activity, adViewContainer)
        }
    }

    private fun loadBannerAdmob(adViewContainer: ViewGroup) {
        adView = AdView(activity)
        adViewContainer.addView(adView)
        adView.adUnitId = activity.getString(R.string.banner_id)
        adView.setAdSize(getAdSize(adViewContainer))
        adView.loadAd(buildAdmobAdRequest(saves))
    }

    private fun loadBannerAppodeal(activity: AppCompatActivity, adViewContainer: ViewGroup) {
        val view = Appodeal.getBannerView(activity)
        adViewContainer.addView(view)
        Appodeal.show(activity, Appodeal.BANNER_VIEW)
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
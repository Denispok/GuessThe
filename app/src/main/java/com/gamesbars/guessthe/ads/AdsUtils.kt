package com.gamesbars.guessthe.ads

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appodeal.ads.Appodeal
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.gamesbars.guessthe.R
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

object AdsUtils {

    const val AD_MEDIATION_TYPE_ADMOB = 0
    const val AD_MEDIATION_TYPE_APPODEAL = 1
    const val AD_MEDIATION_TYPE = AD_MEDIATION_TYPE_APPODEAL

    fun initMobileAds(activity: AppCompatActivity) {
        when (AD_MEDIATION_TYPE) {
            AD_MEDIATION_TYPE_APPODEAL -> initAppodeal(activity)
            AD_MEDIATION_TYPE_ADMOB -> initAdmob(activity)
        }
    }

    private fun initAdmob(context: Context) {
        MobileAds.initialize(context)
    }

    private fun initAppodeal(activity: AppCompatActivity) {
        Appodeal.initialize(
            activity,
            activity.getString(R.string.appodeal_app_id),
            Appodeal.BANNER or Appodeal.INTERSTITIAL or Appodeal.REWARDED_VIDEO,
            object : ApdInitializationCallback {

                override fun onInitializationFinished(errors: List<ApdInitializationError>?) {
                    // TODO
                }
            }
        )
    }

    fun buildAdmobAdRequest(saves: SharedPreferences): AdRequest {
        val adBuilder = AdRequest.Builder()
        if (saves.getBoolean("npa", true)) {
            val extras = Bundle()
            extras.putString("npa", "1")
            adBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }
        return adBuilder.build()
    }
}
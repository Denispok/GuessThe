package com.gamesbars.guessthe.ads.consent

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.appodeal.consent.ConsentManager
import com.appodeal.consent.ConsentStatus
import com.gamesbars.guessthe.ads.AdsAnalytics.logConsentError
import com.google.firebase.analytics.FirebaseAnalytics

object ConsentInfoManager {

    fun showConsentForm(activity: AppCompatActivity) {
        showConsentFormAppodeal(activity)
    }

    fun isUserInConsentZone(): Boolean {
        return ConsentManager.status == ConsentStatus.Obtained || ConsentManager.status == ConsentStatus.Required
    }

    fun updateNpa(context: Context) {
        val npa = !ConsentManager.canShowAds()
        val saves = context.getSharedPreferences("saves", Context.MODE_PRIVATE)
        saves.edit().apply {
            putBoolean("npa", npa)
            apply()
        }

        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseAnalytics.setUserProperty("npa", if (npa) "non-personalized" else "personalized")
    }

    private fun showConsentFormAppodeal(activity: AppCompatActivity) {
        ConsentManager.load(
            context = activity,
            successListener = { consentForm ->
                consentForm.show(activity) { consentManagerError ->
                    updateNpa(activity)
                    consentManagerError?.let {
                        logConsentError("ConsentShowError: ${consentManagerError.message}")
                    }
                }
            },
            failureListener = { error ->
                logConsentError("ConsentLoadingError: ${error.message}")
            },
        )
    }
}
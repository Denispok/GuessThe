package com.gamesbars.guessthe.ads.consent

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.appodeal.consent.Consent
import com.appodeal.consent.ConsentForm
import com.appodeal.consent.ConsentFormListener
import com.appodeal.consent.ConsentInfoUpdateListener
import com.appodeal.consent.ConsentManager
import com.appodeal.consent.ConsentManagerError
import com.gamesbars.guessthe.AnalyticsHelper.logAdsLocation
import com.gamesbars.guessthe.AnalyticsHelper.logConsentError
import com.gamesbars.guessthe.R
import com.google.firebase.analytics.FirebaseAnalytics

object ConsentInfoManager {

    fun showConsentForm(activity: AppCompatActivity, onConsentCompleted: (() -> Unit)? = null) {
        showConsentFormAppodeal(activity, onConsentCompleted)
    }

    fun isUserInConsentZone(): Boolean {
        return ConsentManager.consent.isGDPRScope or ConsentManager.consent.isCCPAScope
    }

    fun nonPersonalizedAdsAppodeal(): Boolean {
        return when (ConsentManager.consentStatus) {
            Consent.Status.PERSONALIZED -> false
            Consent.Status.UNKNOWN -> ConsentManager.shouldShow
            else -> true
        }
    }

    fun putNpa(context: Context, npa: Boolean) {
        val saves = context.getSharedPreferences("saves", Context.MODE_PRIVATE)
        saves.edit().apply {
            putBoolean("npa", npa)
            apply()
        }

        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseAnalytics.setUserProperty("npa", if (npa) "non-personalized" else "personalized")
    }

    /** This method may not call the callback */
    private fun requestConsentUpdateAppodeal(activity: AppCompatActivity, onConsentCompleted: () -> Unit) {
        ConsentManager.requestConsentInfoUpdate(
            activity,
            activity.getString(R.string.appodeal_app_id),
            object : ConsentInfoUpdateListener() {

                override fun onConsentInfoUpdated(consent: Consent) {
                    logAdsLocation(consent.isGDPRScope)
                    when (consent.status) {
                        Consent.Status.PERSONALIZED -> {
                            putNpa(activity, false)
                            onConsentCompleted.invoke()
                        }

                        Consent.Status.UNKNOWN -> {
                            if (ConsentManager.shouldShow) {
                                showConsentFormAppodeal(activity, onConsentCompleted)
                            } else {
                                putNpa(activity, false)
                                onConsentCompleted.invoke()
                            }
                        }

                        else -> {
                            putNpa(activity, true)
                            onConsentCompleted.invoke()
                        }
                    }
                }

                override fun onFailedToUpdateConsentInfo(error: ConsentManagerError) {
                    logConsentError(error.message)
                    putNpa(activity, true)
                    onConsentCompleted.invoke()
                }
            })
    }

    private fun showConsentFormAppodeal(activity: AppCompatActivity, onConsentCompleted: (() -> Unit)? = null) {
        val consentFormListener = object : ConsentFormListener() {

            override fun onConsentFormClosed(consent: Consent) {
                when (consent.status) {
                    Consent.Status.PERSONALIZED -> {
                        putNpa(activity, false)
                        onConsentCompleted?.invoke()
                    }

                    else -> {
                        putNpa(activity, true)
                        onConsentCompleted?.invoke()
                    }
                }

                onConsentCompleted?.invoke()
            }

            override fun onConsentFormError(error: ConsentManagerError) {
                logConsentError("onConsentFormError: ${error.message}")
                onConsentCompleted?.invoke()
            }

            override fun onConsentFormLoaded(consentForm: ConsentForm) {
                consentForm.show()
            }
        }

        val consentForm = ConsentForm(activity, consentFormListener)
        consentForm.load()
    }
}
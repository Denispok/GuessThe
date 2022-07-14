package com.gamesbars.guessthe.ads.consent

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.appodeal.consent.Consent
import com.appodeal.consent.ConsentManager
import com.appodeal.consent.ConsentManagerError
import com.gamesbars.guessthe.AnalyticsHelper.logAdsLocation
import com.gamesbars.guessthe.AnalyticsHelper.logConsentError
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.AdsUtils
import com.gamesbars.guessthe.screen.SplashScreenActivity
import com.google.ads.consent.*
import com.google.firebase.analytics.FirebaseAnalytics
import java.net.URL

object ConsentInfoManager {

    const val CONSENT_INIT_TIME = 5000L

    fun requestConsentUpdate(activity: SplashScreenActivity, onConsentCompleted: () -> Unit) {
        when (AdsUtils.AD_MEDIATION_TYPE) {
            AdsUtils.AD_MEDIATION_TYPE_ADMOB -> requestConsentUpdateAdmob(activity, onConsentCompleted)
            AdsUtils.AD_MEDIATION_TYPE_APPODEAL -> {
                // Consent will be requested automatically
                ConsentManager.addConsentObserver { consent ->
                    logAdsLocation(isUserInConsentZone(activity))
                    when (consent.status) {
                        Consent.Status.PERSONALIZED -> putNpa(activity, false)
                        else -> putNpa(activity, true)
                    }
                }
                onConsentCompleted.invoke()
            }
        }
    }

    fun showConsentForm(activity: AppCompatActivity, onConsentCompleted: (() -> Unit)? = null) {
        when (AdsUtils.AD_MEDIATION_TYPE) {
            AdsUtils.AD_MEDIATION_TYPE_ADMOB -> showConsentFormAdmob(activity, onConsentCompleted)
            AdsUtils.AD_MEDIATION_TYPE_APPODEAL -> showConsentFormAppodeal(activity, onConsentCompleted)
        }
    }

    fun isUserInConsentZone(context: Context): Boolean {
        when (AdsUtils.AD_MEDIATION_TYPE) {
            AdsUtils.AD_MEDIATION_TYPE_ADMOB -> {
                val consentInformation = ConsentInformation.getInstance(context)
                return consentInformation.isRequestLocationInEeaOrUnknown
            }
            AdsUtils.AD_MEDIATION_TYPE_APPODEAL -> {
                return ConsentManager.consent.isGDPRScope or ConsentManager.consent.isCCPAScope
            }
        }
        return true
    }

    fun isUserInConsentZoneAsync(context: Context, onConsentChanged: (isUserInConsentZone: Boolean) -> Unit) {
        when (AdsUtils.AD_MEDIATION_TYPE) {
            AdsUtils.AD_MEDIATION_TYPE_ADMOB -> {
                val consentInformation = ConsentInformation.getInstance(context)
                onConsentChanged.invoke(consentInformation.isRequestLocationInEeaOrUnknown)
            }
            AdsUtils.AD_MEDIATION_TYPE_APPODEAL -> {
                var observer: ((Consent) -> Unit)? = null
                observer = { consent ->
                    onConsentChanged.invoke(consent.isGDPRScope or consent.isCCPAScope)
                    observer?.let { ConsentManager.removeConsentObserver(it) }
                }
                ConsentManager.addConsentObserver(observer)
            }
        }
    }

    private fun requestConsentUpdateAdmob(activity: SplashScreenActivity, onConsentCompleted: () -> Unit) {
        val consentInformation = ConsentInformation.getInstance(activity)
        val publisherIds = arrayOf(activity.getString(R.string.pub_id))
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {

            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                if (consentInformation.isRequestLocationInEeaOrUnknown) {
                    logAdsLocation(true)
                    when (consentStatus) {
                        ConsentStatus.UNKNOWN -> {
                            showConsentFormAdmob(activity, onConsentCompleted)
                            Handler().postDelayed({
                                if (!activity.isConsentLoaded) {
                                    logConsentError("Consent didn't loaded for $CONSENT_INIT_TIME millis")

                                    activity.isConsentTimeOver = true
                                    putNpa(activity, true)
                                    onConsentCompleted.invoke()
                                }
                            }, CONSENT_INIT_TIME)
                        }

                        ConsentStatus.NON_PERSONALIZED -> {
                            putNpa(activity, true)
                            onConsentCompleted.invoke()
                        }

                        ConsentStatus.PERSONALIZED -> {
                            putNpa(activity, false)
                            onConsentCompleted.invoke()
                        }
                    }

                } else {
                    logAdsLocation(false)
                    putNpa(activity, false)
                    onConsentCompleted.invoke()
                }
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                logConsentError("onFailedToUpdateConsentInfo: $errorDescription")
                putNpa(activity, true)
                onConsentCompleted.invoke()
            }
        })
    }

    /** This method may not call the callback */
    private fun requestConsentUpdateAppodeal(activity: AppCompatActivity, onConsentCompleted: () -> Unit) {
        ConsentManager.requestConsentInfoUpdate(
            activity,
            activity.getString(R.string.appodeal_app_id),
            object : com.appodeal.consent.ConsentInfoUpdateListener() {

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

    private fun showConsentFormAdmob(activity: AppCompatActivity, onConsentCompleted: (() -> Unit)? = null) {
        val privacyUrl = URL(activity.getString(R.string.privacy_policy_link))

        var form: ConsentForm? = null
        form = ConsentForm.Builder(activity, privacyUrl)
            .withListener(object : ConsentFormListener() {

                override fun onConsentFormLoaded() {
                    if (activity is SplashScreenActivity) {
                        if (activity.isConsentTimeOver) {
                            logConsentError("onConsentFormLoaded: form loaded after consent time is over")
                            return
                        }
                        activity.isConsentLoaded = true
                    }
                    form?.show()
                }

                override fun onConsentFormClosed(consentStatus: ConsentStatus, userPrefersAdFree: Boolean?) {
                    when (consentStatus) {
                        ConsentStatus.PERSONALIZED -> {
                            putNpa(activity, false)
                            onConsentCompleted?.invoke()
                        }

                        else -> {
                            putNpa(activity, true)
                            onConsentCompleted?.invoke()
                        }
                    }
                }

                override fun onConsentFormError(errorDescription: String) {
                    logConsentError("onConsentFormError: $errorDescription")

                    if (activity is SplashScreenActivity) {
                        if (activity.isConsentTimeOver) return
                        activity.isConsentLoaded = true
                    }

                    putNpa(activity, true)
                    onConsentCompleted?.invoke()
                }
            })
            .withPersonalizedAdsOption()
            .withNonPersonalizedAdsOption()
            .build()

        form.load()
    }

    private fun showConsentFormAppodeal(activity: AppCompatActivity, onConsentCompleted: (() -> Unit)? = null) {
        val consentFormListener = object : com.appodeal.consent.ConsentFormListener() {

            override fun onConsentFormClosed(consent: Consent) {
                Log.d("CONSENT", "onConsentFormClosed тупа")
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

            override fun onConsentFormLoaded(consentForm: com.appodeal.consent.ConsentForm) {
                consentForm.show()
            }
        }

        val consentForm = com.appodeal.consent.ConsentForm(activity, consentFormListener)
        consentForm.load()
    }

    private fun putNpa(context: Context, npa: Boolean) {
        val saves = context.getSharedPreferences("saves", Context.MODE_PRIVATE)
        saves.edit().apply {
            putBoolean("npa", npa)
            apply()
        }

        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseAnalytics.setUserProperty("npa", if (npa) "non-personalized" else "personalized")
    }
}
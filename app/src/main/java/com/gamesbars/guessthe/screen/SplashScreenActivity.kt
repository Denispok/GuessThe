package com.gamesbars.guessthe.screen

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.screen.SplashScreenActivity.Companion.CONSENT_ERROR_TAG
import com.gamesbars.guessthe.sliceUntilIndex
import com.google.ads.consent.*
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import java.net.URL

class SplashScreenActivity : AppCompatActivity() {

    companion object {
        const val CONSENT_ERROR_TAG = "CONSENT"
        const val CONSENT_INIT_TIME = 5000L
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var saves: SharedPreferences

    // FUCKING GOOGLE CONSENT SDK KOSTbIL'
    @Volatile
    var isConsentTimeOver: Boolean = false
    @Volatile
    var isConsentLoaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)

        val consentInformation = ConsentInformation.getInstance(this)
        val publisherIds = arrayOf(getString(R.string.pub_id))
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {

                if (consentInformation.isRequestLocationInEeaOrUnknown) {
                    firebaseAnalytics.setUserProperty("adslocation", "InEeaOrUnknown")
                    when (consentStatus) {
                        ConsentStatus.UNKNOWN -> {
                            showConsentForm(this@SplashScreenActivity)
                            Handler().postDelayed({
                                if (!isConsentLoaded) {
                                    val errorMessage = "Consent didn't loaded for $CONSENT_INIT_TIME millis"
                                    Log.e(CONSENT_ERROR_TAG, errorMessage)

                                    val params = Bundle()
                                    params.putString("message", errorMessage.sliceUntilIndex(99))
                                    firebaseAnalytics.logEvent("consent_error", params)

                                    isConsentTimeOver = true
                                    putNpa(this@SplashScreenActivity, true)
                                    startGame()
                                }
                            }, CONSENT_INIT_TIME)
                        }

                        ConsentStatus.NON_PERSONALIZED -> {
                            putNpa(this@SplashScreenActivity, true)
                            MobileAds.initialize(this@SplashScreenActivity, getString(R.string.ads_id))
                            startGame()
                        }

                        ConsentStatus.PERSONALIZED -> {
                            putNpa(this@SplashScreenActivity, false)
                            MobileAds.initialize(this@SplashScreenActivity, getString(R.string.ads_id))
                            startGame()
                        }
                    }

                } else {
                    firebaseAnalytics.setUserProperty("adslocation", "None")
                    putNpa(this@SplashScreenActivity, false)
                    MobileAds.initialize(this@SplashScreenActivity, this@SplashScreenActivity.getString(R.string.ads_id))
                    startGame()
                }
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                Log.e(CONSENT_ERROR_TAG, errorDescription)

                val params = Bundle()
                params.putString("message", "onFailedToUpdateConsentInfo: $errorDescription".sliceUntilIndex(99))
                firebaseAnalytics.logEvent("consent_error", params)

                putNpa(this@SplashScreenActivity, true)
                MobileAds.initialize(this@SplashScreenActivity, getString(R.string.ads_id))
                startGame()
            }
        })

        if (!saves.contains("russian_carspurchased")) {
            saves.edit().apply {
                putBoolean("russian_carspurchased", true)
                putInt("coins", 10000)
                apply()
            }

            val packs = resources.getStringArray(R.array.packs)
            firebaseAnalytics.setUserProperty(packs[0], "0")
            for (i in 1 until packs.size) {
                firebaseAnalytics.setUserProperty(packs[i], "none")
            }
            firebaseAnalytics.setUserProperty("sound", "on")
        }
    }

    fun startGame() {
        startActivity(Intent(applicationContext, MenuActivity::class.java))
        finish()
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

fun showConsentForm(activity: AppCompatActivity) {
    val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
    val privacyUrl = URL(activity.getString(R.string.privacy_policy_link))

    var form: ConsentForm? = null
    form = ConsentForm.Builder(activity, privacyUrl)
        .withListener(object : ConsentFormListener() {
            override fun onConsentFormLoaded() {
                if (activity is SplashScreenActivity) {
                    if (activity.isConsentTimeOver) {
                        val params = Bundle()
                        params.putString("message", "onConsentFormLoaded: form loaded after consent time is over".sliceUntilIndex(99))
                        firebaseAnalytics.logEvent("consent_error", params)
                        return
                    }
                    activity.isConsentLoaded = true
                }
                form?.show()
            }

            override fun onConsentFormOpened() {
                // Consent form was displayed.
            }

            override fun onConsentFormClosed(consentStatus: ConsentStatus, userPrefersAdFree: Boolean?) {
                when (consentStatus) {
                    ConsentStatus.UNKNOWN -> activity.finish()

                    ConsentStatus.NON_PERSONALIZED -> {
                        putNpa(activity, true)
                        if (activity is SplashScreenActivity) {
                            MobileAds.initialize(activity, activity.getString(R.string.ads_id))
                            activity.startGame()
                        }
                    }

                    ConsentStatus.PERSONALIZED -> {
                        putNpa(activity, false)
                        if (activity is SplashScreenActivity) {
                            MobileAds.initialize(activity, activity.getString(R.string.ads_id))
                            activity.startGame()
                        }
                    }
                }
            }

            override fun onConsentFormError(errorDescription: String) {
                Log.e(CONSENT_ERROR_TAG, errorDescription)

                val params = Bundle()
                params.putString("message", "onConsentFormError: $errorDescription".sliceUntilIndex(99))
                firebaseAnalytics.logEvent("consent_error", params)

                if (activity is SplashScreenActivity) {
                    if (activity.isConsentTimeOver) return
                    activity.isConsentLoaded = true
                }

                putNpa(activity, true)
                if (activity is SplashScreenActivity) {
                    MobileAds.initialize(activity, activity.getString(R.string.ads_id))
                    activity.startGame()
                }
            }
        })
        .withPersonalizedAdsOption()
        .withNonPersonalizedAdsOption()
        .build()

    form.load()
}

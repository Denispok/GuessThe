package com.gamesbars.guessthe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.gamesbars.guessthe.SplashScreenActivity.Companion.CONSENT_ERROR_TAG
import com.google.ads.consent.*
import com.google.android.gms.ads.MobileAds
import java.net.URL

class SplashScreenActivity : AppCompatActivity() {

    companion object {
        const val CONSENT_ERROR_TAG = "CONSENT"
    }

    lateinit var saves: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_splashscreen)

        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)

        val consentInformation = ConsentInformation.getInstance(this)
        val publisherIds = arrayOf(getString(R.string.pub_id))
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                when (consentStatus) {
                    ConsentStatus.UNKNOWN -> showConsentForm(this@SplashScreenActivity)

                    ConsentStatus.NON_PERSONALIZED -> {
                        saves.edit().apply {
                            putBoolean("npa", true)
                            apply()
                        }
                        MobileAds.initialize(this@SplashScreenActivity, getString(R.string.ads_id))
                        startGame()
                    }

                    ConsentStatus.PERSONALIZED -> {
                        saves.edit().apply {
                            putBoolean("npa", false)
                            apply()
                        }
                        MobileAds.initialize(this@SplashScreenActivity, getString(R.string.ads_id))
                        startGame()
                    }
                }
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                Log.e(CONSENT_ERROR_TAG, errorDescription)
                MobileAds.initialize(this@SplashScreenActivity, getString(R.string.ads_id))
                startGame()
            }
        })

        if (!saves.contains("russian_carspurchased"))
            saves.edit().apply {
                putBoolean("russian_carspurchased", true)
                putInt("coins", 10000)
                apply()
            }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    fun startGame() {
        startActivity(Intent(applicationContext, MenuActivity().javaClass))
        finish()
    }
}

fun showConsentForm(activity: AppCompatActivity) {
    val saves = activity.getSharedPreferences("saves", Context.MODE_PRIVATE)
    val privacyUrl = URL(activity.getString(R.string.privacy_policy_link))

    var form: ConsentForm? = null
    form = ConsentForm.Builder(activity, privacyUrl)
        .withListener(object : ConsentFormListener() {
            override fun onConsentFormLoaded() {
                form?.show()
            }

            override fun onConsentFormOpened() {
                // Consent form was displayed.
            }

            override fun onConsentFormClosed(consentStatus: ConsentStatus, userPrefersAdFree: Boolean?) {
                when (consentStatus) {
                    ConsentStatus.UNKNOWN -> activity.finish()

                    ConsentStatus.NON_PERSONALIZED -> {
                        saves.edit().apply {
                            putBoolean("npa", true)
                            apply()
                        }
                        MobileAds.initialize(activity, activity.getString(R.string.ads_id))
                        if (activity is SplashScreenActivity) activity.startGame()
                    }

                    ConsentStatus.PERSONALIZED -> {
                        saves.edit().apply {
                            putBoolean("npa", false)
                            apply()
                        }
                        MobileAds.initialize(activity, activity.getString(R.string.ads_id))
                        if (activity is SplashScreenActivity) activity.startGame()
                    }
                }
            }

            override fun onConsentFormError(errorDescription: String) {
                Log.e(CONSENT_ERROR_TAG, errorDescription)
                MobileAds.initialize(activity, activity.getString(R.string.ads_id))
                if (activity is SplashScreenActivity) activity.startGame()
            }
        })
        .withPersonalizedAdsOption()
        .withNonPersonalizedAdsOption()
        .build()

    form.load()
}

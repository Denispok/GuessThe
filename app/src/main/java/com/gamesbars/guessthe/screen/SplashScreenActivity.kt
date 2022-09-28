package com.gamesbars.guessthe.screen

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.AdsUtils
import com.gamesbars.guessthe.ads.consent.ConsentInfoManager
import com.google.firebase.analytics.FirebaseAnalytics

class SplashScreenActivity : AppCompatActivity() {

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

        ConsentInfoManager.requestConsentUpdate(this, ::startGame)
    }

    private fun startGame() {
        AdsUtils.initMobileAds(this)
        startActivity(Intent(applicationContext, MenuActivity::class.java))
        finish()
    }
}

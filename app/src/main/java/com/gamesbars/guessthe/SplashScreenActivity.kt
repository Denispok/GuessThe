package com.gamesbars.guessthe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_splashscreen)

        MobileAds.initialize(this, getString(R.string.ads_id))

        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        if (!saves.contains("russian_carspurchased"))
            saves.edit().apply {
                putBoolean("russian_carspurchased", true)
                apply()
            }

        Handler().postDelayed({
            startActivity(Intent(applicationContext, MenuActivity().javaClass))
            finish()
        }, 500)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
}

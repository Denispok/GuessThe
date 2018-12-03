package com.gamesbars.guessthe

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

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713")

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

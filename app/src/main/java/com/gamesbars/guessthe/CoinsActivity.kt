package com.gamesbars.guessthe

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class CoinsActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Exo_2/Exo2-Medium.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())

        setContentView(R.layout.activity_coins)
        updateCoins()
    }

    private fun updateCoins() {
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        findViewById<TextView>(R.id.coins_coins).text = saves.getInt("coins", 0).toString()
    }
}

package com.gamesbars.guessthe

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper


class MenuActivity : AppCompatActivity() {

    var isClickable: Boolean = true

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

        hideSystemUI()
        setContentView(R.layout.activity_menu)
        findViewById<TextView>(R.id.menu_rate_coins).text = "+".plus(resources.getInteger(R.integer.rate_reward))
    }

    override fun onResume() {
        super.onResume()
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        if (saves.getBoolean("rated", false))
            findViewById<TextView>(R.id.menu_rate_coins).visibility = View.GONE
        isClickable = true
    }

    fun play(view: View) {
        if (isClickable) {
            isClickable = false
            startActivity(Intent(applicationContext, LevelMenuActivity().javaClass))
        }
    }

    fun shop(view: View) {
        if (isClickable) {
            isClickable = false
            startActivity(Intent(applicationContext, CoinsActivity().javaClass))
        }
    }

    fun rate(view: View) {
        if (isClickable) {
            isClickable = false
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.gamesbars.whatyouchoose")))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_play_link))))
            }
            val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
            if (!saves.getBoolean("rated", false)) {
                val editor = saves.edit()
                editor.putBoolean("rated", true)
                editor.putInt("coins", saves.getInt("coins", 0) + resources.getInteger(R.integer.rate_reward))
                editor.apply()
            }
        }
    }

    fun share(view: View) {
        if (isClickable) {
            isClickable = false
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_text) + getString(R.string.google_play_link))
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_chooser_title)))
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
}

fun AppCompatActivity.hideSystemUI() {
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
}

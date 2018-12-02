package com.gamesbars.guessthe

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_menu.*

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

        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        if (saves.getBoolean("ads", true)) {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
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
            playSound(this, R.raw.button)
            startActivity(Intent(applicationContext, LevelMenuActivity().javaClass))
        }
    }

    fun shop(view: View) {
        if (isClickable) {
            isClickable = false
            playSound(this, R.raw.button)
            startActivity(Intent(applicationContext, CoinsActivity().javaClass))
        }
    }

    fun rate(view: View) {
        if (isClickable) {
            isClickable = false
            playSound(this, R.raw.button)
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

    fun sound(view: View) {
        if (isClickable) {
            val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
            val sound = saves.getBoolean("sound", true)
            saves.edit().apply {
                putBoolean("sound", !sound)
                apply()
            }
            findViewById<ImageView>(R.id.menu_sound).setImageResource(
                    if (sound) R.drawable.baseline_volume_off_white_48
                    else R.drawable.baseline_volume_up_white_48)
        }
    }

    fun share(view: View) {
        if (isClickable) {
            isClickable = false
            playSound(this, R.raw.button)
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

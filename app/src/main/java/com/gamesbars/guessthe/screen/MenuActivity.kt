package com.gamesbars.guessthe.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.buildAdRequest
import com.gamesbars.guessthe.hasConnection
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.coins.CoinsActivity
import com.google.ads.consent.ConsentInformation
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {

    private lateinit var saves: SharedPreferences
    var isClickable: Boolean = true

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        findViewById<TextView>(R.id.menu_rate_coins).text = "+".plus(resources.getInteger(R.integer.rate_reward))
        findViewById<TextView>(R.id.privacy_policy).movementMethod = LinkMovementMethod.getInstance()

        val consentInformation = ConsentInformation.getInstance(this)
        val adsSettingsView = findViewById<TextView>(R.id.ads_settings)
        if (consentInformation.isRequestLocationInEeaOrUnknown) {
            adsSettingsView.setOnClickListener { showConsentForm(this) }
        } else {
            adsSettingsView.visibility = View.GONE
        }

        showBannerAd()

        val sound = saves.getBoolean("sound", true)
        findViewById<ImageView>(R.id.menu_sound).setImageResource(
            if (sound) R.drawable.baseline_volume_up_white_48
            else R.drawable.baseline_volume_off_white_48
        )
    }

    private fun showBannerAd() {
        if (saves.getBoolean("ads", true)) {
            if (hasConnection(this)) {
                adView.visibility = View.VISIBLE
                adView.loadAd(buildAdRequest(saves))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (saves.getBoolean("rated", false))
            findViewById<TextView>(R.id.menu_rate_coins).visibility = View.GONE
        isClickable = true
    }

    fun play(view: View) {
        if (isClickable) {
            isClickable = false
            playSound(this, R.raw.button)
            startActivity(Intent(applicationContext, LevelMenuActivity::class.java))
        }
    }

    fun shop(view: View) {
        if (isClickable) {
            isClickable = false
            playSound(this, R.raw.button)
            startActivity(Intent(applicationContext, CoinsActivity::class.java))
        }
    }

    fun rate(view: View) {
        if (isClickable) {
            isClickable = false
            playSound(this, R.raw.button)

            val params = Bundle()
            params.putString("reward", if (saves.getBoolean("rated", false)) "none" else "${resources.getInteger(R.integer.rate_reward)} coins")
            firebaseAnalytics.logEvent("rate", params)

            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_play_market_link))))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_play_link))))
            }
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
            val sound = saves.getBoolean("sound", true)
            firebaseAnalytics.setUserProperty("sound", if (sound) "off" else "on")
            saves.edit().apply {
                putBoolean("sound", !sound)
                apply()
            }
            findViewById<ImageView>(R.id.menu_sound).setImageResource(
                if (sound) R.drawable.baseline_volume_off_white_48
                else R.drawable.baseline_volume_up_white_48
            )
        }
    }

    fun share(view: View) {
        if (isClickable) {
            isClickable = false
            playSound(this, R.raw.button)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, null)

            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + getString(R.string.google_play_link))
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_chooser_title)))
        }
    }
}
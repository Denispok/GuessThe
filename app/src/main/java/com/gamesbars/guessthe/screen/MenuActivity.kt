package com.gamesbars.guessthe.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.gamesbars.guessthe.AnalyticsHelper
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage
import com.gamesbars.guessthe.ads.AdsUtils
import com.gamesbars.guessthe.ads.BannerAdDelegate
import com.gamesbars.guessthe.ads.consent.ConsentInfoManager
import com.gamesbars.guessthe.databinding.ActivityMenuBinding
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.coins.CoinsActivity
import com.google.firebase.analytics.FirebaseAnalytics

class MenuActivity : AppCompatActivity() {

    private lateinit var saves: SharedPreferences
    var isClickable: Boolean = true

    private lateinit var binding: ActivityMenuBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var bannerAdDelegate: BannerAdDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsUtils.fixDensity(resources)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        bannerAdDelegate = BannerAdDelegate(this, saves)

        binding.rateCoinsTv.text = "+".plus(resources.getInteger(R.integer.rate_reward))
        binding.privacyPolicyTv.movementMethod = LinkMovementMethod.getInstance()
        binding.adsSettingsTv.setOnClickListener { ConsentInfoManager.showConsentForm(this) }

        val sound = saves.getBoolean("sound", true)
        binding.soundIv.setImageResource(
            if (sound) R.drawable.baseline_volume_up_white_48
            else R.drawable.baseline_volume_off_white_48
        )

        if (saves.getBoolean("ads", true)) bannerAdDelegate.loadBanner(this, binding.adViewContainer)
    }

    private fun updateBannerAd() {
        if (saves.getBoolean("ads", true)) {
            binding.adViewContainer.visibility = View.VISIBLE
            bannerAdDelegate.updateBanner(this, binding.adViewContainer)
        } else {
            binding.adViewContainer.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (saves.getBoolean("rated", false)) binding.rateCoinsTv.visibility = View.GONE
        updateConsentInfo()
        updateBannerAd()
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
            val rateReward = resources.getInteger(R.integer.rate_reward)
            params.putString("reward", if (saves.getBoolean("rated", false)) "none" else "$rateReward coins")
            firebaseAnalytics.logEvent("rate", params)

            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_play_market_link))))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_play_link))))
            }
            if (!saves.getBoolean("rated", false)) {
                val editor = saves.edit()
                editor.putBoolean("rated", true)
                editor.apply()
                Storage.addCoins(resources.getInteger(R.integer.rate_reward))
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
            binding.soundIv.setImageResource(
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

    private fun updateConsentInfo() {
        val isUserInConsentZone = ConsentInfoManager.isUserInConsentZone(this)
        // Update location and npa here because consent in Appodeal requests automatically without callback
        if (AdsUtils.AD_MEDIATION_TYPE == AdsUtils.AD_MEDIATION_TYPE_APPODEAL) {
            val npa = ConsentInfoManager.nonPersonalizedAdsAppodeal()
            ConsentInfoManager.putNpa(this, npa)
            AnalyticsHelper.logAdsLocation(isUserInConsentZone)
        }
        binding.adsSettingsTv.isVisible = isUserInConsentZone
    }
}
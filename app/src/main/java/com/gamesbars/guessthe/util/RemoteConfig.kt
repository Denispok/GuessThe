package com.gamesbars.guessthe.util

import com.gamesbars.guessthe.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfig {

    private const val MINIMUM_FETCH_INTERVAL_IN_SECONDS = 1 * 60 * 60L // 1 hour

    private const val NEW_INTERSTITIAL_TIMING_ENABLED = "new_interstitial_timing_enabled"
    private const val INTERSTITIAL_START_DELAY = "interstitial_start_delay"
    private const val INTERSTITIAL_BETWEEN_DELAY = "interstitial_between_delay"
    private const val LEVEL_REWARD = "level_reward"
    private const val RATE_REWARD = "rate_reward"

    private val remoteConfig: FirebaseRemoteConfig get() = Firebase.remoteConfig

    fun init() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = MINIMUM_FETCH_INTERVAL_IN_SECONDS
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
    }

    fun getNewInterstitialTimingEnabled(): Boolean {
        return remoteConfig.getBoolean(NEW_INTERSTITIAL_TIMING_ENABLED)
    }

    fun getInterstitialStartDelay(): Long {
        return remoteConfig.getLong(INTERSTITIAL_START_DELAY)
    }

    fun getInterstitialBetweenDelay(): Long {
        return remoteConfig.getLong(INTERSTITIAL_BETWEEN_DELAY)
    }

    fun getLevelReward(): Int {
        return remoteConfig.getLong(LEVEL_REWARD).toInt()
    }

    fun getRateReward(): Int {
        return remoteConfig.getLong(RATE_REWARD).toInt()
    }
}
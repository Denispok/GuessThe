package com.gamesbars.guessthe.util

import com.gamesbars.guessthe.App
import com.gamesbars.guessthe.R
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.json.JSONArray
import org.xmlpull.v1.XmlPullParser

object RemoteConfig {

    private const val MINIMUM_FETCH_INTERVAL_IN_SECONDS = 1 * 60 * 60L // 1 hour

    private const val NEW_INTERSTITIAL_TIMING_ENABLED = "new_interstitial_timing_enabled"
    private const val INTERSTITIAL_START_DELAY = "interstitial_start_delay"
    private const val INTERSTITIAL_BETWEEN_DELAY = "interstitial_between_delay"
    private const val LEVEL_REWARD = "level_reward"
    private const val RATE_REWARD = "rate_reward"
    private const val PACKS_LEVELS_TO_UNLOCK = "packs_levels_to_unlock"

    private val resources get() = App.appContext.resources
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

    /** Locally we always store array with correct size. If remote config sends us other size we will use local value */
    fun getPacksLevelsToUnlock(): IntArray {
        val defaultValue = getPacksLevelsToUnlockDefault()
        val remoteValue = JSONArray(remoteConfig.getString(PACKS_LEVELS_TO_UNLOCK)).toIntArray()

        if (defaultValue.size != remoteValue.size) {
            val exception = IllegalStateException("Local and remote $PACKS_LEVELS_TO_UNLOCK size are different!")
            Firebase.crashlytics.recordException(exception)
            return defaultValue
        }
        return remoteValue
    }

    private fun getPacksLevelsToUnlockDefault(): IntArray {
        return JSONArray(findEntryValue(PACKS_LEVELS_TO_UNLOCK)).toIntArray()
    }

    private fun findEntryValue(key: String): String {
        val parser = resources.getXml(R.xml.remote_config_defaults)
        var tagName = ""
        var neededEntryFound = false

        var eventType: Int = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                tagName = parser.name
            } else if (eventType == XmlPullParser.TEXT) {
                if (parser.text == key) {
                    neededEntryFound = true
                } else if (neededEntryFound && tagName == "value") {
                    return parser.text
                }
            }
            eventType = parser.next()
        }
        throw IllegalArgumentException("There no such key in remote_config_defaults: $key")
    }

    private fun JSONArray.toIntArray(): IntArray {
        val array = IntArray(length())
        for (i in 0..<length()) {
            array[i] = getInt(i)
        }
        return array
    }
}
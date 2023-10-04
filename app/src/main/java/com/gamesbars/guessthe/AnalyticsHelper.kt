package com.gamesbars.guessthe

import android.os.Bundle
import android.util.Log
import com.gamesbars.guessthe.data.Storage
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsHelper {

    private const val TAG_DEBUG = "DEBUG"
    private const val EVENT_LEVEL_COMPLETE = "level_complete"
    private const val EVENT_PACK_UNLOCKED = "pack_unlocked"

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(App.appContext)

    fun logLevelComplete(pack: String, level: Int) {
        val params = Bundle()
        params.putString("completed_level", (Storage.getLevelName(pack, level)).sliceUntilIndex(99))
        if (BuildConfig.DEBUG) {
            Log.d(TAG_DEBUG, "FA event $EVENT_LEVEL_COMPLETE: $params")
            Log.d(TAG_DEBUG, "FA set user property $pack to $level")
        } else {
            firebaseAnalytics.logEvent(EVENT_LEVEL_COMPLETE, params)
            firebaseAnalytics.setUserProperty(pack, level.toString())
        }
    }

    fun logPackUnlock(pack: String, unlockType: String) {
        val params = Bundle()
        params.putString("pack", pack)
        params.putString("unlock_type", unlockType)
        if (BuildConfig.DEBUG) {
            Log.d(TAG_DEBUG, "FA event $EVENT_PACK_UNLOCKED: $params")
            Log.d(TAG_DEBUG, "FA set user property $pack to 0")
        } else {
            firebaseAnalytics.logEvent(EVENT_PACK_UNLOCKED, params)
            firebaseAnalytics.setUserProperty(pack, "0")
        }
    }
}
package com.gamesbars.guessthe.util

import android.content.Context
import androidx.core.content.edit
import com.gamesbars.guessthe.App
import com.gamesbars.guessthe.BuildConfig

object Migrator {

    private const val PREFERENCES_MIGRATION = "migration"
    private const val KEY_LAST_LAUNCHED_VERSION = "last_launched_version"

    private val migrationPreferences = App.appContext.getSharedPreferences(PREFERENCES_MIGRATION, Context.MODE_PRIVATE)!!

    fun migrate() {
        val lastLaunchedVersion = migrationPreferences.getInt(KEY_LAST_LAUNCHED_VERSION, 0)
        val currentVersion = BuildConfig.VERSION_CODE

        // Migrations here

        if (lastLaunchedVersion != currentVersion) {
            migrationPreferences.edit {
                putInt(KEY_LAST_LAUNCHED_VERSION, currentVersion)
            }
        }
    }
}
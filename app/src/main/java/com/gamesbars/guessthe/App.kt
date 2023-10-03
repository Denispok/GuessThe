package com.gamesbars.guessthe

import android.app.Application
import android.content.Context
import com.gamesbars.guessthe.util.Migrator
import com.gamesbars.guessthe.util.RemoteConfig
import com.gamesbars.guessthe.util.TimeUtil

class App : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        if (!isProcessMain()) return

        TimeUtil.appLaunchTime = TimeUtil.currentTimeMillis()
        RemoteConfig.init()
        Migrator.migrate()
    }
}

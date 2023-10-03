package com.gamesbars.guessthe

import android.app.Application
import android.content.Context
import com.gamesbars.guessthe.util.Migrator

class App : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        Migrator.migrate()
    }
}

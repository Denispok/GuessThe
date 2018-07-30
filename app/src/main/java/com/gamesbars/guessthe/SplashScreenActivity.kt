package com.gamesbars.guessthe

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import java.io.IOException


class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        val dBHelper = DataBaseHelper(this)

        try {
            dBHelper.createDataBase()
        } catch (ioe: IOException) {
            throw Error("Unable to create database")
        }

        Handler().postDelayed({
            startActivity(Intent(applicationContext, MenuActivity().javaClass))
            finish()
        }, 500)
    }
}
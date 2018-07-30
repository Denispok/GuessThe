package com.gamesbars.guessthe

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
    }

    fun play(view: View) {
        startActivity(Intent(applicationContext, LevelMenuActivity().javaClass))
    }
}
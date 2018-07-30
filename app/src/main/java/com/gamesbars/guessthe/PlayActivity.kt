package com.gamesbars.guessthe

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class PlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        supportFragmentManager.beginTransaction()
                .replace(R.id.activity_play,
                        LevelFragment.newInstance(intent.extras.getString("pack")))
                .commit()
    }
}
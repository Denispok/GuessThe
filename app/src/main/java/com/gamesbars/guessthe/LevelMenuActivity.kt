package com.gamesbars.guessthe

import android.content.Intent
import android.database.SQLException
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout

class LevelMenuActivity : AppCompatActivity() {

    companion object {
        const val PACK_LEVELS_COUNT = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levelmenu)
        loadPacks()
    }

    private fun loadPacks() {
        val packsNames = resources.getStringArray(R.array.packs_names)
        val packs = resources.getStringArray(R.array.packs)
        val packsList = findViewById<LinearLayout>(R.id.levels_list)
        for (id in 0 until packsNames.size) {
            val button = Button(this)
            button.text = packsNames[id]
            button.setOnClickListener {
                val intent = Intent(this, PlayActivity().javaClass)
                intent.putExtra("pack", packs[id])
                startActivity(intent)
            }
            packsList.addView(button)
        }
    }
}
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
        val dBHelper = DataBaseHelper(this)

        try {
            dBHelper.openDataBase()
        } catch (sqle: SQLException) {
            throw sqle
        }

        val cursor = dBHelper.db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
        val packsNames = arrayListOf<String>()

        while (cursor.moveToNext()) {
            val tableName = cursor.getString(0)
            if (!tableName.equals("android_metadata")) packsNames.add(tableName)
        }

        cursor.close()
        dBHelper.close()

        val packsList = findViewById<LinearLayout>(R.id.levels_list)
        for (pack in packsNames) {
            val button = Button(this)
            button.text = pack
            button.setOnClickListener {
                val intent = Intent(this, PlayActivity().javaClass)
                intent.putExtra("pack", pack)
                startActivity(intent)
            }
            packsList.addView(button)
        }
    }
}
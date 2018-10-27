package com.gamesbars.guessthe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class LevelMenuActivity : AppCompatActivity() {

    companion object {
        const val PACK_LEVELS_COUNT = 10
    }

    var isClickable: Boolean = true

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Exo_2/Exo2-Medium.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())

        setContentView(R.layout.activity_levelmenu)
        loadPacks()
        findViewById<ImageView>(R.id.levelmenu_back).setOnClickListener { this.onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        updateCoins()
        updateProgressBars()
        isClickable = true
    }

    private fun updateCoins() {
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        findViewById<TextView>(R.id.levelmenu_coins).text = saves.getInt("coins", 0).toString()
    }

    private fun loadPacks() {
        val packsNames = resources.getStringArray(R.array.packs_names)
        val packs = resources.getStringArray(R.array.packs)
        val packsList = findViewById<LinearLayout>(R.id.levels_list)
        for (id in 0 until packsNames.size) {
            val button = layoutInflater.inflate(R.layout.button_levelmenu, packsList, false)
            button.findViewById<TextView>(R.id.levelmenu_button_pack_name).text = packsNames[id]
            button.setOnClickListener {
                if (isClickable) {
                    isClickable = false
                    val intent = Intent(this, PlayActivity().javaClass)
                    intent.putExtra("pack", packs[id])
                    startActivity(intent)
                }
            }
            packsList.addView(button)
        }
    }

    private fun updateProgressBars() {
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        val packs = resources.getStringArray(R.array.packs)
        val packsList = findViewById<LinearLayout>(R.id.levels_list)
        for (id in 0 until packsList.childCount) {
            val currentButton = packsList.getChildAt(id)
            val completedPercent = (saves.getInt(packs[id] + "completed", 0).toFloat() / PACK_LEVELS_COUNT * 100).toInt()
            currentButton.findViewById<ProgressBar>(R.id.levelmenu_button_progress_bar).progress = completedPercent
            currentButton.findViewById<TextView>(R.id.levelmenu_button_progress_bar_text).text =
                    if (completedPercent.equals(100)) getString(R.string.completed)
                    else getString(R.string.percent, saves.getInt(packs[id] + "completed", 0), PACK_LEVELS_COUNT)
        }
    }
}

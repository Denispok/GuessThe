package com.gamesbars.guessthe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.gamesbars.guessthe.fragment.ConfirmDialogFragment
import com.gamesbars.guessthe.fragment.InternetConnectionDialog
import com.google.android.gms.ads.AdRequest
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_levelmenu.*

class LevelMenuActivity : AppCompatActivity() {

    companion object {
        const val PACK_LEVELS_COUNT = 10
    }

    var isClickable: Boolean = true
    var currentDialog: ConfirmDialogFragment? = null

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

        hideSystemUI()
        setContentView(R.layout.activity_levelmenu)
        loadPacks()
        findViewById<ImageView>(R.id.levelmenu_back).setOnClickListener {
            if (isClickable) {
                playSound(this, R.raw.button)
                this.onBackPressed()
            }
        }

        findViewById<TextView>(R.id.levelmenu_coins).setOnClickListener {
            if (isClickable) {
                isClickable = false
                startActivity(Intent(applicationContext, CoinsActivity().javaClass))
            }
        }

        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        if (saves.getBoolean("ads", true)) {
            if (hasConnection(this)) {
                if (hasConnection(this)) {
                    val adRequest = AdRequest.Builder().build()
                    adView.visibility = View.VISIBLE
                    adView.loadAd(adRequest)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateCoins()
        updateProgressBars()
        isClickable = true
    }

    fun updateCoins() {
        val coins = getSharedPreferences("saves", Context.MODE_PRIVATE).getInt("coins", 0).toString()
        findViewById<TextView>(R.id.levelmenu_coins).text = coins
    }

    private fun loadPacks() {
        val packsNames = resources.getStringArray(R.array.packs_names)
        val packsList = findViewById<LinearLayout>(R.id.levels_list)
        for (id in 0 until packsNames.size) {
            val button = layoutInflater.inflate(R.layout.button_levelmenu, packsList, false)
            button.findViewById<TextView>(R.id.levelmenu_button_pack_name).text = packsNames[id]
            packsList.addView(button)
        }
    }

    fun updateProgressBars() {
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        val packs = resources.getStringArray(R.array.packs)
        val packsList = findViewById<LinearLayout>(R.id.levels_list)
        for (id in 0 until packsList.childCount) {
            val currentButton = packsList.getChildAt(id)
            val progressBarText = currentButton.findViewById<TextView>(R.id.levelmenu_button_progress_bar_text)
            if (saves.getBoolean(packs[id] + "purchased", false)) {
                val completedLevels = saves.getInt(packs[id] + "completed", 0)
                val completedPercent = ((completedLevels + 1).toFloat() / PACK_LEVELS_COUNT * 100).toInt()
                currentButton.findViewById<ProgressBar>(R.id.levelmenu_button_progress_bar).progress = completedPercent
                progressBarText.text =
                        if (completedLevels == PACK_LEVELS_COUNT) getString(R.string.completed)
                        else getString(R.string.percent, completedLevels + 1, PACK_LEVELS_COUNT)
                progressBarText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                currentButton.setOnClickListener {
                    if (isClickable) {
                        isClickable = false

                        fun startLevel() {
                            val intent = Intent(this, PlayActivity().javaClass)
                            intent.putExtra("pack", packs[id])
                            startActivity(intent)
                        }

                        if (hasConnection(this)) {
                            if (saves.getInt("without_connection", 0) != 0)
                                saves.edit().apply {
                                    putInt("without_connection", 0)
                                    apply()
                                }
                            startLevel()
                        } else {
                            saves.edit().apply {
                                putInt("without_connection", saves.getInt("without_connection", 0) + 1)
                                apply()
                            }
                            if (saves.getInt("without_connection", 0) >= 3) {
                                InternetConnectionDialog().show(supportFragmentManager, getString(R.string.internet_connection_dialog_fragment_tag))
                                isClickable = true
                            } else
                                startLevel()
                        }
                    }
                }
            } else {
                progressBarText.text = getString(R.string.buy_levels, resources.getIntArray(R.array.packs_prices)[id])
                progressBarText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.coin_icon_16, 0)
                currentButton.setOnClickListener {
                    if (isClickable) {
                        isClickable = false
                        playSound(this, R.raw.button)
                        currentDialog = ConfirmDialogFragment.newInstance(id, packs[id])
                        currentDialog!!.show(supportFragmentManager, getString(R.string.confirm_dialog_fragment_tag))
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onBackPressed() {
        currentDialog?.apply {
            updateActivity()
            dismiss()
        } ?: super.onBackPressed()
    }
}

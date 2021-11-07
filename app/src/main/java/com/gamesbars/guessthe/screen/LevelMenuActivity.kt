package com.gamesbars.guessthe.screen

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.BannerAdDelegate
import com.gamesbars.guessthe.fragment.ConfirmDialogFragment
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.coins.CoinsActivity
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_levelmenu.*

class LevelMenuActivity : AppCompatActivity() {

    private lateinit var saves: SharedPreferences
    var isClickable: Boolean = true
    var currentDialog: ConfirmDialogFragment? = null

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var bannerAdDelegate: BannerAdDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levelmenu)

        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        bannerAdDelegate = BannerAdDelegate(this, saves)

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
                startActivity(Intent(applicationContext, CoinsActivity::class.java))
            }
        }

        if (saves.getBoolean("ads", true)) {
            adViewContainer.visibility = View.VISIBLE
            bannerAdDelegate.loadBanner(adViewContainer)
        }
    }

    override fun onResume() {
        super.onResume()
        updateCoins()
        updateProgressBars()
        isClickable = true
    }

    fun updateCoins() {
        val coins = saves.getInt("coins", 0).toString()
        findViewById<TextView>(R.id.levelmenu_coins).text = coins
        firebaseAnalytics.setUserProperty("coins", coins)
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
        val packs = resources.getStringArray(R.array.packs)
        val packsSizes = resources.getIntArray(R.array.packs_sizes)
        val packPrices = resources.getIntArray(R.array.packs_prices)
        val packsList = findViewById<LinearLayout>(R.id.levels_list)

        for (id in 0 until packsList.childCount) {
            val currentButton = packsList.getChildAt(id)
            val progressBarText = currentButton.findViewById<TextView>(R.id.levelmenu_button_progress_bar_text)
            if (saves.getBoolean(packs[id] + "purchased", false)) {
                val completedLevels = saves.getInt(packs[id] + "completed", 0)
                val levelCount = packsSizes[id]
                val completedPercent = ((completedLevels + 1).toFloat() / levelCount * 100).toInt()
                currentButton.findViewById<ProgressBar>(R.id.levelmenu_button_progress_bar).progress = completedPercent
                progressBarText.text =
                    if (completedLevels == levelCount) getString(R.string.completed)
                    else getString(R.string.percent, completedLevels + 1, levelCount)
                progressBarText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                currentButton.setOnClickListener {
                    if (isClickable) {
                        isClickable = false

                        val intent = Intent(this, PlayActivity::class.java)
                        intent.putExtra("pack", packs[id])
                        startActivity(intent)
                    }
                }
            } else {
                progressBarText.text = getString(R.string.buy_levels, packPrices[id])
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

    override fun onBackPressed() {
        currentDialog?.apply {
            updateActivity()
            dismiss()
        } ?: super.onBackPressed()
    }
}
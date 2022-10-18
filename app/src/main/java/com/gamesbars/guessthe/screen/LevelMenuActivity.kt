package com.gamesbars.guessthe.screen

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage
import com.gamesbars.guessthe.ads.AdsUtils
import com.gamesbars.guessthe.ads.BannerAdDelegate
import com.gamesbars.guessthe.databinding.ActivityLevelmenuBinding
import com.gamesbars.guessthe.databinding.ButtonLevelmenuBinding
import com.gamesbars.guessthe.fragment.ConfirmDialogFragment
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.coins.CoinsActivity
import com.google.firebase.analytics.FirebaseAnalytics

class LevelMenuActivity : AppCompatActivity() {

    private lateinit var saves: SharedPreferences
    var isClickable: Boolean = true
    var currentDialog: ConfirmDialogFragment? = null

    private lateinit var binding: ActivityLevelmenuBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var bannerAdDelegate: BannerAdDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsUtils.fixDensity(resources)
        binding = ActivityLevelmenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        bannerAdDelegate = BannerAdDelegate(this, saves)

        loadPacks()

        binding.backIv.setOnClickListener {
            if (isClickable) {
                playSound(this, R.raw.button)
                this.onBackPressed()
            }
        }

        binding.coinsTv.setOnClickListener {
            if (isClickable) {
                isClickable = false
                startActivity(Intent(applicationContext, CoinsActivity::class.java))
            }
        }

        if (saves.getBoolean("ads", true)) bannerAdDelegate.loadBanner(this, binding.adViewContainer)
    }

    override fun onResume() {
        super.onResume()
        updateCoins()
        updateProgressBars()
        updateBannerAd()
        isClickable = true
    }

    fun updateCoins() {
        val coins = Storage.getCoins().toString()
        binding.coinsTv.text = coins
        firebaseAnalytics.setUserProperty("coins", coins)
    }

    private fun loadPacks() {
        val packsNames = resources.getStringArray(R.array.packs_names)
        val packsList = binding.levelsLl
        for (id in 0 until packsNames.size) {
            val buttonBinding = ButtonLevelmenuBinding.inflate(layoutInflater, packsList, false)
            buttonBinding.packNameTv.text = packsNames[id]
            packsList.addView(buttonBinding.root)
        }
    }

    private fun updateBannerAd() {
        if (saves.getBoolean("ads", true)) {
            binding.adViewContainer.visibility = View.VISIBLE
            bannerAdDelegate.updateBanner(this, binding.adViewContainer)
        } else {
            binding.adViewContainer.visibility = View.GONE
        }
    }

    fun updateProgressBars() {
        val packs = resources.getStringArray(R.array.packs)
        val packsSizes = resources.getIntArray(R.array.packs_sizes)
        val packPrices = resources.getIntArray(R.array.packs_prices)
        val packsList = binding.levelsLl

        for (id in 0 until packsList.childCount) {
            val currentButton = packsList.getChildAt(id)
            val currentButtonBinding = ButtonLevelmenuBinding.bind(currentButton)
            val progressBarText = currentButtonBinding.progressTv
            val currentPack = packs[id]
            if (Storage.isPackOpen(currentPack, id)) {
                val completedLevels = Storage.getCompletedLevels(currentPack)
                val levelCount = packsSizes[id]
                val completedPercent = ((completedLevels + 1).toFloat() / levelCount * 100).toInt()
                currentButtonBinding.progressPb.progress = completedPercent
                progressBarText.text =
                    if (completedLevels == levelCount) getString(R.string.completed)
                    else getString(R.string.percent, completedLevels + 1, levelCount)
                progressBarText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                progressBarText.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topMargin = 0
                }
                currentButtonBinding.completeLevelsTv.isVisible = false
                currentButton.setOnClickListener {
                    if (isClickable) {
                        isClickable = false

                        val intent = Intent(this, PlayActivity::class.java)
                        intent.putExtra("pack", currentPack)
                        startActivity(intent)
                    }
                }
            } else {
                progressBarText.text = getString(R.string.buy_levels, packPrices[id])
                progressBarText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.coin_icon_16, 0)

                val levelsRemainingToUnlock = Storage.getLevelsRemainingToUnlock(id)
                if (levelsRemainingToUnlock != null) {
                    progressBarText.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        topMargin = resources.getDimension(R.dimen.levelmenu_progress_bar_text_margin).toInt()
                    }
                    currentButtonBinding.completeLevelsTv.isVisible = true
                    currentButtonBinding.completeLevelsTv.text = resources.getQuantityString(
                        R.plurals.complete_levels_to_unlock, levelsRemainingToUnlock, levelsRemainingToUnlock
                    )
                } else {
                    progressBarText.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        topMargin = 0
                    }
                    currentButtonBinding.completeLevelsTv.isVisible = false
                }

                currentButton.setOnClickListener {
                    if (isClickable) {
                        isClickable = false
                        playSound(this, R.raw.button)
                        currentDialog = ConfirmDialogFragment.newInstance(id, currentPack)
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